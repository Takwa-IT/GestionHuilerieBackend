package Services;

import Config.ReferenceUtils;
import Mapper.LotOlivesMapper;
import Models.CampagneOlives;
import Models.LotOlives;
import Models.MatierePremiere;
import Models.Stock;
import Models.Utilisateur;
import Repositories.CampagneOlivesRepository;
import Repositories.HuilerieRepository;
import Repositories.LotOlivesRepository;
import Repositories.StockRepository;
import dto.LotArrivageCreateDTO;
import dto.LotOlivesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LotOlivesService {

    private final LotOlivesRepository lotOlivesRepository;
    private final CampagneOlivesRepository campagneOlivesRepository;
    private final HuilerieRepository huilerieRepository;
    private final StockRepository stockRepository;
    private final LotOlivesMapper lotOlivesMapper;
    private final MatierePremiereService matierePremiereService;
    private final StockMovementService stockMovementService;
    private final CurrentUserService currentUserService;

    @Value("${app.storage.bon-pesee-dir:generated/bons-pesee}")
    private String bonPeseeDir;

    @Transactional
    public LotOlivesDTO createArrivage(LotArrivageCreateDTO dto) {
        Long effectiveHuilerieId = resolveEffectiveHuilerieId(dto.getHuilerieId());

        if (dto.getMatierePremiereReference() == null || dto.getMatierePremiereReference().isBlank() ||
                dto.getCampagneReference() == null || dto.getCampagneReference().isBlank()) {
            throw new RuntimeException("Matiere premiere et campagne obligatoires pour creer un lot");
        }

        MatierePremiere matierePremiere = matierePremiereService.findMatiere(dto.getMatierePremiereReference());
        CampagneOlives campagne = campagneOlivesRepository.findByReference(dto.getCampagneReference())
                .orElseThrow(() -> new RuntimeException("Campagne non trouvee"));

        ensureRelationsMatchHuilerie(effectiveHuilerieId, matierePremiere, campagne);

        LotOlives lot = new LotOlives();
        lot.setReference("TMP-LO-" + java.util.UUID.randomUUID());
        lot.setVariete(dto.getVariete());
        lot.setMaturite(dto.getMaturite());
        lot.setOrigine(dto.getOrigine());
        lot.setDateRecolte(dto.getDateRecolte());
        lot.setDateReception(dto.getDateReception());
        lot.setFournisseurNom(dto.getFournisseurNom());
        lot.setFournisseurCIN(dto.getFournisseurCIN());
        lot.setDureeStockageAvantBroyage(dto.getDureeStockageAvantBroyage());
        lot.setPesee(dto.getPesee());
        lot.setQuantiteInitiale(dto.getPesee());
        lot.setQuantiteRestante(dto.getPesee());
        lot.setMatierePremiere(matierePremiere);
        lot.setCampagne(campagne);
        lot.setHuilerie(huilerieRepository.findById(effectiveHuilerieId)
                .orElseThrow(() -> new RuntimeException("Huilerie non trouvee")));

        LotOlives savedLot = lotOlivesRepository.save(lot);
        savedLot.setReference(ReferenceUtils.format("LO", savedLot.getIdLot()));
        savedLot = lotOlivesRepository.save(savedLot);
        final LotOlives persistedLot = savedLot;

        // Une ligne stock représente une matière première dans une huilerie
        // On récupère ou crée le stock consolidé pour cette matière première
        Stock stock = stockRepository.findByLotOlives_Huilerie_IdHuilerieAndLotOlives_MatierePremiere_Id(
                effectiveHuilerieId,
                matierePremiere.getId())
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setTypeStock(matierePremiere.getType());
                    newStock.setQuantiteDisponible(0d);
                    newStock.setLotOlives(persistedLot);
                    Stock savedStock = stockRepository.save(newStock);
                    savedStock.setReference(ReferenceUtils.format("ST", savedStock.getIdStock()));
                    return stockRepository.save(savedStock);
                });

        // Met à jour lot_id à titre informatif (traçabilité du dernier lot)
        stock.setLotOlives(persistedLot);

        stockMovementService.createArrivalForStock(
                stock,
                persistedLot,
                dto.getPesee(),
                dto.getDateReception(),
                "Arrivage lot " + savedLot.getReference());

        writeBonPeseePdf(savedLot);

        return lotOlivesMapper.toDTO(savedLot);
    }

    public byte[] downloadBonPesee(Long idLot) {
        LotOlives lot = findLot(idLot);
        Path pdfPath = resolvePdfPath(lot);
        if (Files.exists(pdfPath)) {
            try {
                return Files.readAllBytes(pdfPath);
            } catch (IOException e) {
                throw new RuntimeException("Lecture du PDF impossible", e);
            }
        }
        return writeBonPeseePdf(lot);
    }

    public List<LotOlivesDTO> findAll(String huilerieNom) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            List<LotOlives> lots = hasText(huilerieNom)
                    ? lotOlivesRepository.findAllByHuilerieNom(huilerieNom)
                    : lotOlivesRepository.findAll();
            List<Long> accessibleHuilerieIds = currentUserService.getAccessibleHuilerieIds();
            return lots.stream()
                    .filter(lot -> lot.getHuilerie() != null
                            && accessibleHuilerieIds.contains(lot.getHuilerie().getIdHuilerie()))
                    .map(lotOlivesMapper::toDTO)
                    .toList();
        }

        Long huilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        return lotOlivesRepository.findAllByHuilerieId(huilerieId).stream().map(lotOlivesMapper::toDTO).toList();
    }

    public LotOlivesDTO findById(Long idLot) {
        return lotOlivesMapper.toDTO(findLot(idLot));
    }

    public LotOlives findLot(Long idLot) {
        return lotOlivesRepository.findById(idLot)
                .orElseThrow(() -> new RuntimeException("Lot non trouve"));
    }

    private Long resolveEffectiveHuilerieId(Long requestedHuilerieId) {
        Utilisateur utilisateur = currentUserService.getAuthenticatedUtilisateur();
        if (currentUserService.isAdmin(utilisateur)) {
            if (requestedHuilerieId == null) {
                throw new RuntimeException("huilerieId est obligatoire pour l'arrivage");
            }
            currentUserService.ensureCanAccessHuilerie(requestedHuilerieId);
            return requestedHuilerieId;
        }

        Long currentHuilerieId = currentUserService.getCurrentHuilerieIdOrThrow();
        if (requestedHuilerieId != null && !currentHuilerieId.equals(requestedHuilerieId)) {
            throw new AccessDeniedException("Acces refuse a une autre huilerie");
        }
        return currentHuilerieId;
    }

    private void ensureRelationsMatchHuilerie(Long huilerieId, MatierePremiere matierePremiere,
            CampagneOlives campagne) {
        Long matiereHuilerieId = matierePremiere != null && matierePremiere.getHuilerie() != null
                ? matierePremiere.getHuilerie().getIdHuilerie()
                : null;
        Long campagneHuilerieId = campagne != null && campagne.getHuilerie() != null
                ? campagne.getHuilerie().getIdHuilerie()
                : null;

        if (matiereHuilerieId != null && !huilerieId.equals(matiereHuilerieId)) {
            throw new RuntimeException("Matiere premiere non autorisee pour cette huilerie");
        }

        if (campagneHuilerieId != null && !huilerieId.equals(campagneHuilerieId)) {
            throw new RuntimeException("Campagne non autorisee pour cette huilerie");
        }
    }

    private byte[] writeBonPeseePdf(LotOlives lot) {
        Path pdfPath = resolvePdfPath(lot);
        byte[] pdf = buildBonPeseePdf(lot);

        try {
            Files.createDirectories(pdfPath.getParent());
            Files.write(pdfPath, pdf);
            return pdf;
        } catch (IOException e) {
            throw new RuntimeException("Enregistrement du PDF impossible", e);
        }
    }

    private Path resolvePdfPath(LotOlives lot) {
        String reference = lot.getReference();
        return Paths.get(resolveBonPeseeDir(), "bon-pesee-" + sanitizeReference(reference) + ".pdf");
    }

    private String resolveBonPeseeDir() {
        if (bonPeseeDir == null || bonPeseeDir.isBlank()) {
            return "generated/bons-pesee";
        }
        return bonPeseeDir;
    }

    private String sanitizeReference(String reference) {
        return reference == null ? String.valueOf(System.currentTimeMillis())
                : reference.replaceAll("[^a-zA-Z0-9-_]", "_");
    }

    private byte[] buildBonPeseePdf(LotOlives lot) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        Font title = new Font(Font.HELVETICA, 20, Font.BOLD, new Color(20, 66, 107));
        Font subtitle = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(80, 80, 80));
        Font section = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(20, 66, 107));
        Font label = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(60, 60, 60));
        Font value = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font footer = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(90, 90, 90));

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Paragraph header = new Paragraph("BON DE PESEE", title);
            header.setAlignment(Element.ALIGN_CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("Reception et controle du lot d'olives", subtitle);
            subHeader.setAlignment(Element.ALIGN_CENTER);
            subHeader.setSpacingAfter(14f);
            document.add(subHeader);

            PdfPTable metaTable = new PdfPTable(2);
            metaTable.setWidthPercentage(100);
            metaTable.setSpacingAfter(12f);
            metaTable.setWidths(new float[] { 1.3f, 2.7f });
            addLabeledCell(metaTable, "Reference lot", nullSafe(lot.getReference()), label, value);
            addLabeledCell(metaTable, "Date reception", nullSafe(lot.getDateReception()), label, value);
            addLabeledCell(metaTable, "Campagne",
                    lot.getCampagne() != null ? nullSafe(lot.getCampagne().getReference()) : "-", label, value);
            addLabeledCell(metaTable, "Matiere premiere",
                    lot.getMatierePremiere() != null ? nullSafe(lot.getMatierePremiere().getReference()) : "-", label,
                    value);
            document.add(metaTable);

            Paragraph lotSection = new Paragraph("Details du lot", section);
            lotSection.setSpacingAfter(6f);
            document.add(lotSection);

            PdfPTable lotTable = new PdfPTable(2);
            lotTable.setWidthPercentage(100);
            lotTable.setSpacingAfter(12f);
            lotTable.setWidths(new float[] { 1.3f, 2.7f });
            addLabeledCell(lotTable, "Variete", nullSafe(lot.getVariete()), label, value);
            addLabeledCell(lotTable, "Maturite", nullSafe(lot.getMaturite()), label, value);
            addLabeledCell(lotTable, "Origine", nullSafe(lot.getOrigine()), label, value);
            addLabeledCell(lotTable, "Date recolte", nullSafe(lot.getDateRecolte()), label, value);
            addLabeledCell(lotTable, "Duree avant broyage", (lot.getDureeStockageAvantBroyage() == null ? "-"
                    : lot.getDureeStockageAvantBroyage() + " jour(s)"), label, value);
            addLabeledCell(lotTable, "Pesee nette",
                    String.format(java.util.Locale.US, "%.2f kg", lot.getPesee() == null ? 0d : lot.getPesee()), label,
                    value);
            document.add(lotTable);

            Paragraph supplierSection = new Paragraph("Fournisseur", section);
            supplierSection.setSpacingAfter(6f);
            document.add(supplierSection);

            PdfPTable supplierTable = new PdfPTable(2);
            supplierTable.setWidthPercentage(100);
            supplierTable.setWidths(new float[] { 1.3f, 2.7f });
            addLabeledCell(supplierTable, "Nom", nullSafe(lot.getFournisseurNom()), label, value);
            addLabeledCell(supplierTable, "CIN", nullSafe(lot.getFournisseurCIN()), label, value);
            document.add(supplierTable);

            Paragraph footerText = new Paragraph("Document genere automatiquement par le systeme de tracabilite.",
                    footer);
            footerText.setSpacingBefore(20f);
            footerText.setAlignment(Element.ALIGN_RIGHT);
            document.add(footerText);

            document.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Generation du PDF impossible", e);
        }
    }

    private void addLabeledCell(PdfPTable table, String fieldLabel, String fieldValue, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(fieldLabel, labelFont));
        labelCell.setBackgroundColor(new Color(240, 245, 250));
        labelCell.setPadding(8f);
        labelCell.setBorderColor(new Color(210, 220, 230));
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(fieldValue, valueFont));
        valueCell.setPadding(8f);
        valueCell.setBorderColor(new Color(210, 220, 230));
        table.addCell(valueCell);
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
