package Services;

import Config.ReferenceUtils;
import Mapper.PeseeMapper;
import Models.CampagneOlives;
import Models.LotOlives;
import Models.Pesee;
import Models.Stock;
import Models.Utilisateur;
import Repositories.CampagneOlivesRepository;
import Repositories.HuilerieRepository;
import Repositories.LotOlivesRepository;
import Repositories.PeseeRepository;
import Repositories.StockRepository;
import com.lowagie.text.*;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import java.awt.Color;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import dto.PeseeDTO;
import dto.ReceptionPeseeCreateDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PeseeService {

    private final PeseeRepository peseeRepository;
    private final LotOlivesRepository lotOlivesRepository;
    private final MatierePremiereService matierePremiereService;
    private final CampagneOlivesRepository campagneOlivesRepository;
    private final HuilerieRepository huilerieRepository;
    private final StockRepository stockRepository;
    private final StockMovementService stockMovementService;
    private final PeseeMapper peseeMapper;
    private final CurrentUserService currentUserService;

    @Value("${app.storage.bon-pesee-dir:generated/bons-pesee}")
    private String bonPeseeDir;

    public PeseeDTO createReception(ReceptionPeseeCreateDTO dto) {
        Long effectiveHuilerieId = resolveEffectiveHuilerieId(dto.getHuilerieId());
        double poidsTare = dto.getPoidsTare() == null ? 0d : dto.getPoidsTare();
        double poidsNet = dto.getPoidsBrut() - poidsTare;

        if (poidsNet <= 0) {
            throw new RuntimeException("Le poids net doit etre strictement positif");
        }

        LotOlives lot = resolveLot(dto, poidsNet);

        Pesee pesee = new Pesee();
        pesee.setReference("TMP-PS-" + UUID.randomUUID());
        pesee.setDatePesee(dto.getDatePesee());
        pesee.setPoidsBrut(dto.getPoidsBrut());
        pesee.setPoidsTare(poidsTare);
        pesee.setPoidsNet(poidsNet);
        pesee.setLot(lot);
        Pesee savedPesee = peseeRepository.save(pesee);
        savedPesee.setReference(ReferenceUtils.format("PS", savedPesee.getId()));
        savedPesee.setBonPeseePdfPath(buildPdfRelativePath(savedPesee.getReference()));
        savedPesee = peseeRepository.save(savedPesee);
        writeBonPeseePdf(savedPesee);

        Stock stock = resolveStock(effectiveHuilerieId, lot);
        stockMovementService.createArrivalForStock(
                stock,
                poidsNet,
                dto.getDatePesee(),
                "Reception lot " + lot.getIdLot()
        );
        return peseeMapper.toDTO(savedPesee);
    }

    public PeseeDTO updateReception(Long id, ReceptionPeseeCreateDTO dto) {
        Pesee pesee = peseeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pesee non trouvee"));

        Long effectiveHuilerieId = resolveEffectiveHuilerieId(dto.getHuilerieId());
        ensurePeseeInCurrentHuilerie(pesee, effectiveHuilerieId);

        double poidsTare = dto.getPoidsTare() == null ? 0d : dto.getPoidsTare();
        double poidsNet = dto.getPoidsBrut() - poidsTare;

        if (poidsNet <= 0) {
            throw new RuntimeException("Le poids net doit etre strictement positif");
        }

        LotOlives ancienLot = pesee.getLot();
        double ancienPoidsNet = safe(pesee.getPoidsNet());

        adjustLotQuantities(ancienLot, -ancienPoidsNet);
        adjustStockQuantity(effectiveHuilerieId, ancienLot, -ancienPoidsNet);

        try {
            LotOlives lot = resolveLot(dto, poidsNet);
            pesee.setDatePesee(dto.getDatePesee());
            pesee.setPoidsBrut(dto.getPoidsBrut());
            pesee.setPoidsTare(poidsTare);
            pesee.setPoidsNet(poidsNet);
            pesee.setLot(lot);
            Pesee savedPesee = peseeRepository.save(pesee);
            writeBonPeseePdf(savedPesee);
            return peseeMapper.toDTO(savedPesee);
        } catch (RuntimeException ex) {
            adjustLotQuantities(ancienLot, ancienPoidsNet);
            adjustStockQuantity(effectiveHuilerieId, ancienLot, ancienPoidsNet);
            throw ex;
        }
    }

    public void deleteReception(Long id) {
        Pesee pesee = peseeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pesee non trouvee"));

        LotOlives lot = pesee.getLot();
        double poidsNet = safe(pesee.getPoidsNet());
        Long huilerieId = resolveHuilerieId(lot);

        adjustLotQuantities(lot, -poidsNet);
        adjustStockQuantity(huilerieId, lot, -poidsNet);
        deletePdfIfExists(pesee);
        peseeRepository.delete(pesee);
    }

    public PeseeDTO findByReference(String reference) {
        return peseeMapper.toDTO(findPesee(reference));
    }

    public List<PeseeDTO> findAll() {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            return peseeRepository.findAllByOrderByDatePeseeDesc().stream().map(this::toDTOWithHuilerieId).toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return peseeRepository.findAllByHuilerie_IdHuilerieOrderByDatePeseeDesc(huilerieId)
                .stream()
                .map(this::toDTOWithHuilerieId)
                .toList();
    }

    public byte[] generateBonPeseePdf(String reference) {
        Pesee pesee = findPesee(reference);
        Path pdfPath = resolvePdfPath(pesee);

        if (Files.exists(pdfPath)) {
            try {
                return Files.readAllBytes(pdfPath);
            } catch (IOException e) {
                throw new RuntimeException("Lecture du PDF impossible", e);
            }
        }

        return writeBonPeseePdf(pesee);
    }

    private byte[] writeBonPeseePdf(Pesee pesee) {
        Path pdfPath = resolvePdfPath(pesee);
        byte[] pdf = buildBonPeseePdf(pesee);

        try {
            Files.createDirectories(pdfPath.getParent());
            Files.write(pdfPath, pdf);
            if (!pdfPath.toString().equals(pesee.getBonPeseePdfPath())) {
                pesee.setBonPeseePdfPath(pdfPath.toString().replace('\\', '/'));
                peseeRepository.save(pesee);
            }
            return pdf;
        } catch (IOException e) {
            throw new RuntimeException("Enregistrement du PDF impossible", e);
        }
    }

    private byte[] buildBonPeseePdf(Pesee pesee) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        Color primary = new Color(54, 84, 61);
        Color secondary = new Color(240, 235, 220);
        Color light = new Color(250, 250, 250);
        Color accent = new Color(221, 235, 204);
        Color dark = new Color(60, 60, 60);

        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, primary);
        Font subtitleFont = new Font(Font.HELVETICA, 11, Font.NORMAL, dark);
        Font sectionFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD, dark);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font highlightFont = new Font(Font.HELVETICA, 12, Font.BOLD, primary);
        Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(110, 110, 110));

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            addHeader(document, pesee, titleFont, subtitleFont);
            addSectionTitle(document, "Informations Du Lot", sectionFont, primary);
            addLotInfoTable(document, pesee, labelFont, valueFont, secondary, light);

            addSectionTitle(document, "Informations De Pesee", sectionFont, primary);
            addPeseeInfoTable(document, pesee, labelFont, valueFont, secondary, light);

            addSectionTitle(document, "Resume Des Poids", sectionFont, primary);
            addPoidsTable(document, pesee, sectionFont, valueFont, accent, light, highlightFont);

            Paragraph footer = new Paragraph(
                    "Document genere automatiquement par le systeme de gestion de l'huilerie.",
                    footerFont
            );
            footer.setSpacingBefore(24f);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Generation du PDF impossible", e);
        }
    }

    private Path resolvePdfPath(Pesee pesee) {
        String relativePath = hasText(pesee.getBonPeseePdfPath())
                ? pesee.getBonPeseePdfPath()
                : buildPdfRelativePath(pesee.getReference());
        return Paths.get(relativePath);
    }

    private String buildPdfRelativePath(String reference) {
        return Paths.get(resolveBonPeseeDir(), "bon-pesee-" + sanitizeReference(reference) + ".pdf")
                .toString()
                .replace('\\', '/');
    }

    private String resolveBonPeseeDir() {
        if (bonPeseeDir == null || bonPeseeDir.isBlank()) {
            return "generated/bons-pesee";
        }
        return bonPeseeDir;
    }

    private String sanitizeReference(String reference) {
        return reference == null ? UUID.randomUUID().toString() : reference.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private void addHeader(Document document, Pesee pesee, Font titleFont, Font subtitleFont) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{2f, 3f});
        headerTable.setSpacingAfter(16f);

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);

        Paragraph huilerie = new Paragraph("HUILERIE", titleFont);
        huilerie.setSpacingAfter(6f);
        leftCell.addElement(huilerie);
        leftCell.addElement(new Paragraph("Bon de pesee officiel", subtitleFont));

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph title = new Paragraph("BON DE PESEE", titleFont);
        title.setAlignment(Element.ALIGN_RIGHT);
        title.setSpacingAfter(6f);
        rightCell.addElement(title);

        Paragraph referenceText = new Paragraph("Reference : " + pesee.getReference(), subtitleFont);
        referenceText.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(referenceText);

        Paragraph date = new Paragraph("Date : " + pesee.getDatePesee(), subtitleFont);
        date.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(date);

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);

        document.add(headerTable);
    }

    private void addSectionTitle(Document document, String title, Font font, Color bgColor) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6f);
        table.setSpacingAfter(8f);

        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8f);
        cell.setBorder(Rectangle.NO_BORDER);

        table.addCell(cell);
        document.add(table);
    }

    private void addLotInfoTable(
            Document document,
            Pesee pesee,
            Font labelFont,
            Font valueFont,
            Color labelBg,
            Color valueBg
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3f});
        table.setSpacingAfter(10f);

        addInfoCell(table, "ID Lot", labelFont, labelBg, Element.ALIGN_LEFT);
        addInfoCell(table, String.valueOf(pesee.getLot().getIdLot()), valueFont, valueBg, Element.ALIGN_LEFT);

        addInfoCell(table, "Variete", labelFont, labelBg, Element.ALIGN_LEFT);
        addInfoCell(table, nullSafe(pesee.getLot().getVarieteOlive()), valueFont, valueBg, Element.ALIGN_LEFT);

        addInfoCell(table, "Origine", labelFont, labelBg, Element.ALIGN_LEFT);
        addInfoCell(table, nullSafe(pesee.getLot().getOrigine()), valueFont, valueBg, Element.ALIGN_LEFT);

        addInfoCell(table, "Date Reception", labelFont, labelBg, Element.ALIGN_LEFT);
        addInfoCell(table, nullSafe(pesee.getLot().getDateReception()), valueFont, valueBg, Element.ALIGN_LEFT);

        document.add(table);
    }

    private void addPeseeInfoTable(
            Document document,
            Pesee pesee,
            Font labelFont,
            Font valueFont,
            Color labelBg,
            Color valueBg
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 3f});
        table.setSpacingAfter(10f);

        addInfoCell(table, "Reference", labelFont, labelBg, Element.ALIGN_LEFT);
        addInfoCell(table, nullSafe(pesee.getReference()), valueFont, valueBg, Element.ALIGN_LEFT);

        addInfoCell(table, "Date Pesee", labelFont, labelBg, Element.ALIGN_LEFT);
        addInfoCell(table, nullSafe(pesee.getDatePesee()), valueFont, valueBg, Element.ALIGN_LEFT);

        document.add(table);
    }

    private void addPoidsTable(
            Document document,
            Pesee pesee,
            Font headerFont,
            Font valueFont,
            Color highlightBg,
            Color normalBg,
            Font highlightFont
    ) throws DocumentException {
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 1f, 1f});
        table.setSpacingAfter(10f);

        addInfoCell(table, "Poids Brut (kg)", headerFont, new Color(54, 84, 61), Element.ALIGN_CENTER);
        addInfoCell(table, "Poids Tare (kg)", headerFont, new Color(54, 84, 61), Element.ALIGN_CENTER);
        addInfoCell(table, "Poids Net (kg)", headerFont, new Color(54, 84, 61), Element.ALIGN_CENTER);

        addInfoCell(table, formatNumber(pesee.getPoidsBrut()), valueFont, normalBg, Element.ALIGN_CENTER);
        addInfoCell(table, formatNumber(pesee.getPoidsTare()), valueFont, normalBg, Element.ALIGN_CENTER);

        PdfPCell netCell = new PdfPCell(new Phrase(formatNumber(pesee.getPoidsNet()), highlightFont));
        netCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        netCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        netCell.setPadding(10f);
        netCell.setBackgroundColor(highlightBg);
        netCell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(netCell);

        document.add(table);
    }

    private void addInfoCell(PdfPTable table, String text, Font font, Color bgColor, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(8f);
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(Color.LIGHT_GRAY);
        table.addCell(cell);
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private String formatNumber(Double value) {
        return value == null ? "0" : String.format("%.2f", value);
    }

    //gère le lot
    //si lot existant -> contrôle + mise à jour
    //si nouveau lot -> création
    private LotOlives resolveLot(ReceptionPeseeCreateDTO dto, double poidsNet) {
        if(dto.getLotId() != null) {
            LotOlives existing = lotOlivesRepository.findById(dto.getLotId())
                    .orElseThrow(() -> new RuntimeException("Lot non trouve"));

            if (hasText(dto.getOrigine()) && !normalize(dto.getOrigine()).equals(normalize(existing.getOrigine()))) {
                throw new RuntimeException("Origine incoherente avec le lot existant");
            }
            if (hasText(dto.getVarieteOlive()) && !normalize(dto.getVarieteOlive()).equals(normalize(existing.getVarieteOlive()))) {
                throw new RuntimeException("Variete incoherente avec le lot existant");
            }

            existing.setQuantiteInitiale(safe(existing.getQuantiteInitiale()) + poidsNet);
            existing.setQuantiteRestante(safe(existing.getQuantiteRestante()) + poidsNet);
            return lotOlivesRepository.save(existing);
        }

        if (dto.getMatierePremiereId() == null || dto.getCampagneAnnee() == null) {
            throw new RuntimeException("Matiere premiere et campagne obligatoires pour creer un nouveau lot");
        }

        CampagneOlives campagne = campagneOlivesRepository.findByAnnee(dto.getCampagneAnnee())
                .orElseThrow(() -> new RuntimeException("Campagne non trouvee"));

        LotOlives lot = new LotOlives();
        lot.setVarieteOlive(dto.getVarieteOlive());
        lot.setMaturite(dto.getMaturite());
        lot.setOrigine(dto.getOrigine());
        lot.setDateRecolte(dto.getDateRecolte());
        lot.setDateReception(dto.getDateReception());
        lot.setDureeStockageAvantBroyage(dto.getDureeStockageAvantBroyage());
        lot.setQuantiteInitiale(poidsNet);
        lot.setQuantiteRestante(poidsNet);
        lot.setMatierePremiere(matierePremiereService.findMatiere(dto.getMatierePremiereId()));
        lot.setCampagne(campagne);
        LotOlives savedLot = lotOlivesRepository.save(lot);
        savedLot.setReference(ReferenceUtils.format("LO", savedLot.getIdLot()));
        return lotOlivesRepository.save(savedLot);
    }

    //gère le stock :
    //si stock existant -> récupération
    //si stock absent -> création
    private Stock resolveStock(Long huilerieId, LotOlives lot) {
        if (huilerieId == null) {
            throw new RuntimeException("huilerieId est obligatoire pour la reception");
        }

        return stockRepository.findByHuilerie_IdHuilerieAndLotOlives_IdLot(huilerieId, lot.getIdLot())
                .orElseGet(() -> {
                    Stock stock = new Stock();
                    stock.setReference(null);
                    stock.setTypeStock("LOT_OLIVES");
                    stock.setQuantiteDisponible(0d);
                    stock.setLotOlives(lot);
                    stock.setHuilerie(huilerieRepository.findById(huilerieId)
                            .orElseThrow(() -> new RuntimeException("Huilerie non trouvee")));
                    Stock savedStock = stockRepository.save(stock);
                    savedStock.setReference(ReferenceUtils.format("ST", savedStock.getIdStock()));
                    return stockRepository.save(savedStock);
                });
    }

    private void adjustLotQuantities(LotOlives lot, double delta) {
        lot.setQuantiteInitiale(Math.max(0d, safe(lot.getQuantiteInitiale()) + delta));
        lot.setQuantiteRestante(Math.max(0d, safe(lot.getQuantiteRestante()) + delta));
        lotOlivesRepository.save(lot);
    }

    private void adjustStockQuantity(Long huilerieId, LotOlives lot, double delta) {
        if (huilerieId == null || lot == null || lot.getIdLot() == null) {
            return;
        }

        stockRepository.findByHuilerie_IdHuilerieAndLotOlives_IdLot(huilerieId, lot.getIdLot())
                .ifPresent(stock -> {
                    stock.setQuantiteDisponible(Math.max(0d, safe(stock.getQuantiteDisponible()) + delta));
                    stockRepository.save(stock);
                });
    }

    private Long resolveHuilerieId(LotOlives lot) {
        if (lot == null || lot.getIdLot() == null) {
            return null;
        }

        return stockRepository.findByLotOlives_IdLot(lot.getIdLot()).stream()
                .map(Stock::getHuilerie)
                .filter(huilerie -> huilerie != null && huilerie.getIdHuilerie() != null)
                .map(huilerie -> huilerie.getIdHuilerie())
                .findFirst()
                .orElse(null);
    }

    private void deletePdfIfExists(Pesee pesee) {
        try {
            Files.deleteIfExists(resolvePdfPath(pesee));
        } catch (IOException ignored) {
            // La suppression de la pesee reste prioritaire meme si le fichier PDF n'est pas supprimable.
        }
    }

    //recupere un pesee par ID + utilisable dans des autres methodes
    private Pesee findPesee(String reference) {
        return peseeRepository.findByReference(reference)
                .orElseThrow(() -> new RuntimeException("Pesee non trouvee"));
    }

    //evite les NULL sur les quantites
    private double safe(Double value) {
        return value == null ? 0d : value;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private PeseeDTO toDTOWithHuilerieId(Pesee pesee) {
        PeseeDTO dto = peseeMapper.toDTO(pesee);
        if (dto.getHuilerieId() == null) {
            dto.setHuilerieId(resolveHuilerieId(pesee.getLot()));
        }
        return dto;
    }

    private Long resolveEffectiveHuilerieId(Long requestedHuilerieId) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            if (requestedHuilerieId == null) {
                throw new RuntimeException("huilerieId est obligatoire pour la reception");
            }
            return requestedHuilerieId;
        }

        Long currentHuilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        if (requestedHuilerieId != null && !currentHuilerieId.equals(requestedHuilerieId)) {
            throw new AccessDeniedException("Acces refuse a une autre huilerie");
        }
        return currentHuilerieId;
    }

    private void ensurePeseeInCurrentHuilerie(Pesee pesee, Long effectiveHuilerieId) {
        if (pesee == null || pesee.getLot() == null || pesee.getLot().getIdLot() == null) {
            return;
        }

        boolean inScope = stockRepository
                .findByLotOlives_IdLotAndHuilerie_IdHuilerie(pesee.getLot().getIdLot(), effectiveHuilerieId)
                .stream()
                .findAny()
                .isPresent();

        if (!inScope) {
            throw new AccessDeniedException("Acces refuse a une pesee d'une autre huilerie");
        }
    }
}
