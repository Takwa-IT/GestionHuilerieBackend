package Services;

import Config.ReferenceUtils;
import Config.LotDeletionException;
import Mapper.LotOlivesMapper;
import Models.CampagneOlives;
import Models.LotOlives;
import Models.MatierePremiere;
import Models.Stock;
import Models.Utilisateur;
import Repositories.CampagneOlivesRepository;
import Repositories.HuilerieRepository;
import Repositories.LotOlivesRepository;
import Repositories.AnalyseLaboratoireRepository;
import Repositories.StockRepository;
import dto.LotArrivageCreateDTO;
import dto.LotOlivesDTO;
import dto.LotOlivesUpdateDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final AnalyseLaboratoireRepository analyseLaboratoireRepository;
    private final LotOlivesMapper lotOlivesMapper;
    private final MatierePremiereService matierePremiereService;
    private final StockMovementService stockMovementService;
    private final CurrentUserService currentUserService;

    @Value("${app.storage.bon-pesee-dir:generated/bons-pesee}")
    private String bonPeseeDir;

    private static final Color BON_PESEE_PRIMARY = new Color(46, 125, 50);
    private static final Color BON_PESEE_SECONDARY = new Color(232, 245, 233);
    private static final Color BON_PESEE_TEXT = new Color(31, 42, 31);

    @Transactional
    public LotOlivesDTO createArrivage(LotArrivageCreateDTO dto) {
        Long effectiveHuilerieId = resolveEffectiveHuilerieId(dto.getHuilerieId());
        String variete = resolveVariete(dto);
        String fournisseurNom = normalizeSupplierValue(dto.getFournisseurNom());
        String fournisseurCIN = normalizeSupplierValue(dto.getFournisseurCIN());

        String matiereReference = dto.getMatierePremiereReference() == null
                ? ""
                : dto.getMatierePremiereReference().trim();
        String campagneReference = dto.getCampagneReference() == null
                ? ""
                : dto.getCampagneReference().trim();

        if (matiereReference.isBlank() || campagneReference.isBlank()) {
            throw new RuntimeException("Matiere premiere et campagne obligatoires pour creer un lot");
        }

        MatierePremiere matierePremiere = matierePremiereService.findMatiere(matiereReference);
        CampagneOlives campagne = resolveCampagne(campagneReference);

        ensureRelationsMatchHuilerie(effectiveHuilerieId, matierePremiere, campagne);
        validateSupplierIdentity(fournisseurNom, fournisseurCIN, null);

        LotOlives lot = new LotOlives();
        lot.setReference("TMP-LO-" + java.util.UUID.randomUUID());
        lot.setVariete(variete);
        lot.setMaturite(dto.getMaturite());
        lot.setOrigine(dto.getOrigine());
        lot.setRegion(dto.getRegion());
        lot.setMethodeRecolte(dto.getMethodeRecolte());
        lot.setTypeSol(dto.getTypeSol());
        lot.setTempsDepuisRecolteHeures(dto.getTempsDepuisRecolteHeures());
        lot.setHumiditePourcent(dto.getHumiditePourcent());
        lot.setAciditeOlivesPourcent(dto.getAciditeOlivesPourcent());
        lot.setTauxFeuillesPourcent(dto.getTauxFeuillesPourcent());
        lot.setLavageEffectue(normalizeLavageEffectue(dto.getLavageEffectue()));
        lot.setDateRecolte(dto.getDateRecolte());
        lot.setDateReception(dto.getDateReception());
        lot.setFournisseurNom(fournisseurNom);
        lot.setFournisseurCIN(fournisseurCIN);
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
        String varieteNormalisee = variete.trim().toLowerCase();

        Stock stock = stockRepository.findByLotOlives_Huilerie_IdHuilerieAndVariete(
                        effectiveHuilerieId,
                        varieteNormalisee)
                .orElseGet(() -> {
                    Stock newStock = new Stock();
                    newStock.setTypeStock(matierePremiere.getType());
                    newStock.setVariete(varieteNormalisee);
                    newStock.setQuantiteDisponible(0d);
                    newStock.setLotOlives(persistedLot);
                    Stock savedStock = stockRepository.save(newStock);
                    savedStock.setReference(ReferenceUtils.format("ST", savedStock.getIdStock()));
                    return stockRepository.save(savedStock);
                });

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

    private String resolveVariete(LotArrivageCreateDTO dto) {
        if (dto == null) {
            throw new RuntimeException("La variété du lot est obligatoire");
        }

        String variete = dto.getVariete();
        if (variete == null || variete.trim().isEmpty()) {
            throw new RuntimeException("La variété du lot est obligatoire");
        }

        return variete.trim();
    }

    public byte[] generateBonPeseePdf(String reference) {
        LotOlives lot = findLotByReference(reference);
        return downloadOrGenerateBonPesee(lot);
    }

    public byte[] downloadBonPesee(Long idLot) {
        LotOlives lot = findLot(idLot);
        return downloadOrGenerateBonPesee(lot);
    }

    private byte[] downloadOrGenerateBonPesee(LotOlives lot) {
        Path pdfPath = resolvePdfPath(lot);
        if (Files.exists(pdfPath)) {
            ensureBonPeseePathPersisted(lot, pdfPath);
            try {
                return Files.readAllBytes(pdfPath);
            } catch (IOException e) {
                throw new RuntimeException("Lecture du PDF impossible", e);
            }
        }
        return writeBonPeseePdf(lot);
    }

    public LotOlivesDTO uploadBonPeseePdf(Long idLot, org.springframework.web.multipart.MultipartFile file) {
        LotOlives lot = findLot(idLot);
        if (file.isEmpty()) {
            throw new RuntimeException("Fichier vide");
        }

        String contentType = file.getContentType();
        if (contentType == null || !"application/pdf".equalsIgnoreCase(contentType)) {
            throw new RuntimeException("Seuls les fichiers PDF sont acceptes");
        }

        try {
            String fileName = "bon-pesee-" + sanitizeReference(lot.getReference()) + ".pdf";
            Path bonPeseeDir = Paths.get(resolveBonPeseeDir());
            Files.createDirectories(bonPeseeDir);

            Path filePath = bonPeseeDir.resolve(fileName);
            file.transferTo(filePath);

            String normalizedPath = filePath.toString().replace('\\', '/');
            lot.setBonPeseePdfPath(normalizedPath);
            lotOlivesRepository.save(lot);

            return lotOlivesMapper.toDTO(lot);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du PDF", e);
        }
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

    @Transactional
    public LotOlivesDTO update(Long idLot, LotOlivesUpdateDTO dto) {
        LotOlives lot = findLot(idLot);
        String supplierNom = dto.getFournisseurNom() != null ? normalizeSupplierValue(dto.getFournisseurNom()) : lot.getFournisseurNom();
        String supplierCIN = dto.getFournisseurCIN() != null ? normalizeSupplierValue(dto.getFournisseurCIN()) : lot.getFournisseurCIN();

        if (dto.getVariete() != null && !dto.getVariete().trim().isEmpty()) {
            lot.setVariete(dto.getVariete().trim());
        }
        if (dto.getMaturite() != null) {
            lot.setMaturite(dto.getMaturite());
        }
        if (dto.getOrigine() != null) {
            lot.setOrigine(dto.getOrigine());
        }
        if (dto.getRegion() != null) {
            lot.setRegion(dto.getRegion());
        }
        if (dto.getMethodeRecolte() != null) {
            lot.setMethodeRecolte(dto.getMethodeRecolte());
        }
        if (dto.getTypeSol() != null) {
            lot.setTypeSol(dto.getTypeSol());
        }
        if (dto.getTempsDepuisRecolteHeures() != null) {
            lot.setTempsDepuisRecolteHeures(dto.getTempsDepuisRecolteHeures());
        }
        if (dto.getHumiditePourcent() != null) {
            lot.setHumiditePourcent(dto.getHumiditePourcent());
        }
        if (dto.getAciditeOlivesPourcent() != null) {
            lot.setAciditeOlivesPourcent(dto.getAciditeOlivesPourcent());
        }
        if (dto.getTauxFeuillesPourcent() != null) {
            lot.setTauxFeuillesPourcent(dto.getTauxFeuillesPourcent());
        }
        if (dto.getLavageEffectue() != null) {
            lot.setLavageEffectue(normalizeLavageEffectue(dto.getLavageEffectue()));
        }
        if (dto.getDateRecolte() != null) {
            lot.setDateRecolte(dto.getDateRecolte());
        }
        if (dto.getDateReception() != null) {
            lot.setDateReception(dto.getDateReception());
        }
        if (dto.getDureeStockageAvantBroyage() != null) {
            lot.setDureeStockageAvantBroyage(dto.getDureeStockageAvantBroyage());
        }
        if (dto.getPesee() != null) {
            lot.setPesee(dto.getPesee());
        }
        if (dto.getQuantiteInitiale() != null) {
            lot.setQuantiteInitiale(dto.getQuantiteInitiale());
        }
        if (dto.getQuantiteRestante() != null) {
            lot.setQuantiteRestante(dto.getQuantiteRestante());
        }

        if (dto.getMatierePremiereReference() != null && !dto.getMatierePremiereReference().trim().isEmpty()) {
            MatierePremiere matierePremiere = matierePremiereService
                    .findMatiere(dto.getMatierePremiereReference().trim());
            lot.setMatierePremiere(matierePremiere);
        }

        if (dto.getCampagneReference() != null && !dto.getCampagneReference().trim().isEmpty()) {
            CampagneOlives campagne = resolveCampagne(dto.getCampagneReference().trim());
            lot.setCampagne(campagne);
        }

        if (dto.getFournisseurNom() != null) {
            lot.setFournisseurNom(supplierNom);
        }
        if (dto.getFournisseurCIN() != null) {
            lot.setFournisseurCIN(supplierCIN);
        }

        Long huilerieId = lot.getHuilerie() != null ? lot.getHuilerie().getIdHuilerie() : null;
        ensureRelationsMatchHuilerie(huilerieId, lot.getMatierePremiere(), lot.getCampagne());
        validateSupplierIdentity(lot.getFournisseurNom(), lot.getFournisseurCIN(), lot.getIdLot());

        LotOlives saved = lotOlivesRepository.save(lot);
        return lotOlivesMapper.toDTO(saved);
    }

    private String normalizeLavageEffectue(String value) {
        if (!hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private String normalizeSupplierValue(String value) {
        if (!hasText(value)) {
            return null;
        }

        return value.trim();
    }

    private void validateSupplierIdentity(String fournisseurNom, String fournisseurCIN, Long currentLotId) {
        String normalizedNom = normalizeSupplierValue(fournisseurNom);
        String normalizedCIN = normalizeSupplierValue(fournisseurCIN);

        if (!hasText(normalizedNom) && !hasText(normalizedCIN)) {
            return;
        }

        if (!hasText(normalizedNom) || !hasText(normalizedCIN)) {
            throw new RuntimeException("Le fournisseur doit avoir un nom et un CIN renseignés.");
        }

        List<LotOlives> lotsByCin = lotOlivesRepository.findByFournisseurCINIgnoreCase(normalizedCIN);
        for (LotOlives existing : lotsByCin) {
            if (currentLotId != null && currentLotId.equals(existing.getIdLot())) {
                continue;
            }

            String existingNom = normalizeSupplierValue(existing.getFournisseurNom());
            if (!hasText(existingNom) || !existingNom.equalsIgnoreCase(normalizedNom)) {
                throw new RuntimeException("Ce CIN fournisseur est déjà associé à un autre fournisseur.");
            }
        }

        List<LotOlives> lotsByNom = lotOlivesRepository.findByFournisseurNomIgnoreCase(normalizedNom);
        for (LotOlives existing : lotsByNom) {
            if (currentLotId != null && currentLotId.equals(existing.getIdLot())) {
                continue;
            }

            String existingCIN = normalizeSupplierValue(existing.getFournisseurCIN());
            if (!hasText(existingCIN) || !existingCIN.equalsIgnoreCase(normalizedCIN)) {
                throw new RuntimeException("Ce fournisseur est déjà associé à un autre CIN.");
            }
        }
    }

    @Transactional
    public void delete(Long idLot) {
        LotOlives lot = findLot(idLot);
        try {
            // First, remove linked laboratory analyses to avoid transient reference errors
            var analyses = analyseLaboratoireRepository.findByLot_IdLotOrderByDateAnalyseAsc(idLot);
            if (!analyses.isEmpty()) {
                analyseLaboratoireRepository.deleteAll(analyses);
                analyseLaboratoireRepository.flush();

                // Clear the reference in the lot entity to prevent Hibernate transient errors
                lot.setAnalyseLaboratoire(null);
            }

            lotOlivesRepository.delete(lot);
            lotOlivesRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            String reason = "Ce lot est lié à d'autres données (production, stock, analyses) et ne peut pas être supprimé.";
            Throwable cause = ex.getMostSpecificCause();
            if (cause != null && cause.getMessage() != null) {
                String msg = cause.getMessage().toLowerCase();
                if (msg.contains("production")) {
                    reason = "Ce lot est lié à des exécutions de production en cours. Impossible de le supprimer.";
                } else if (msg.contains("stock")) {
                    reason = "Ce lot a du stock associé. Veuillez d'abord vider le stock avant suppression.";
                } else if (msg.contains("mouvement")) {
                    reason = "Ce lot a des mouvements de stock associés. Impossible de le supprimer.";
                }
            }
            throw new LotDeletionException(
                    "Impossible de supprimer ce lot",
                    reason);
        }
    }

    public LotOlives findLot(Long idLot) {
        LotOlives lot = lotOlivesRepository.findById(idLot)
                .orElseThrow(() -> new EntityNotFoundException("Lot non trouve"));
        Long huilerieId = lot.getHuilerie() != null ? lot.getHuilerie().getIdHuilerie() : null;
        currentUserService.ensureCanAccessHuilerie(huilerieId);
        return lot;
    }

    private LotOlives findLotByReference(String reference) {
        return lotOlivesRepository.findByReference(reference)
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

    private CampagneOlives resolveCampagne(String candidate) {
        String raw = candidate == null ? "" : candidate.trim();
        if (raw.isBlank()) {
            throw new RuntimeException("Campagne non trouvee");
        }

        var foundByReference = campagneOlivesRepository.findByNormalizedReference(raw);
        if (foundByReference.isPresent()) {
            return foundByReference.get();
        }

        Long extractedId = extractNumericId(raw);
        if (extractedId != null) {
            var foundById = campagneOlivesRepository.findById(extractedId);
            if (foundById.isPresent()) {
                return foundById.get();
            }
        }

        var foundByYear = campagneOlivesRepository.findByAnnee(raw);
        if (foundByYear.isPresent()) {
            return foundByYear.get();
        }

        String lookupKey = normalizeLookupKey(raw);
        return campagneOlivesRepository.findAll().stream()
                .filter(c -> lookupKey.equals(normalizeLookupKey(c.getReference()))
                        || lookupKey.equals(normalizeLookupKey(c.getAnnee())))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Campagne non trouvee"));
    }

    private Long extractNumericId(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String digitsOnly = value.replaceAll("^\\D+", "");
        if (digitsOnly.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(digitsOnly);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String normalizeLookupKey(String value) {
        if (value == null) {
            return "";
        }

        return value
                .trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]", "");
    }

    private byte[] writeBonPeseePdf(LotOlives lot) {
        Path pdfPath = resolvePdfPath(lot);
        byte[] pdf = buildBonPeseePdf(lot);

        try {
            Files.createDirectories(pdfPath.getParent());
            Files.write(pdfPath, pdf);
            ensureBonPeseePathPersisted(lot, pdfPath);
            return pdf;
        } catch (IOException e) {
            throw new RuntimeException("Enregistrement du PDF impossible", e);
        }
    }

    private Path resolvePdfPath(LotOlives lot) {
        if (hasText(lot.getBonPeseePdfPath())) {
            return Paths.get(lot.getBonPeseePdfPath());
        }
        String reference = lot.getReference();
        return Paths.get(resolveBonPeseeDir(), "bon-pesee-" + sanitizeReference(reference) + ".pdf");
    }

    private void ensureBonPeseePathPersisted(LotOlives lot, Path pdfPath) {
        String normalizedPath = pdfPath.toString().replace('\\', '/');
        if (!normalizedPath.equals(lot.getBonPeseePdfPath())) {
            lot.setBonPeseePdfPath(normalizedPath);
            lotOlivesRepository.save(lot);
        }
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

        Color primary = BON_PESEE_PRIMARY;
        Color secondary = BON_PESEE_SECONDARY;
        Color dark = BON_PESEE_TEXT;
        Color border = mix(primary, Color.WHITE, 0.72f);
        Color labelBackground = mix(secondary, Color.WHITE, 0.35f);
        Color valueBackground = Color.WHITE;
        Color subtitleColor = mix(primary, dark, 0.55f);

        Font title = new Font(Font.HELVETICA, 18, Font.BOLD, primary);
        Font subtitle = new Font(Font.HELVETICA, 11, Font.NORMAL, subtitleColor);
        Font section = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        Font label = new Font(Font.HELVETICA, 10, Font.BOLD, dark);
        Font value = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font footer = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(110, 110, 110));

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
            addLabeledCell(metaTable, "Reference lot", nullSafe(lot.getReference()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(metaTable, "Date reception", nullSafe(lot.getDateReception()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(metaTable, "Campagne",
                    lot.getCampagne() != null ? nullSafe(lot.getCampagne().getReference()) : "-", label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(metaTable, "Matiere premiere",
                    lot.getMatierePremiere() != null ? nullSafe(lot.getMatierePremiere().getReference()) : "-", label,
                    value, labelBackground, valueBackground, border);
            document.add(metaTable);

            addSectionBanner(document, "Details du lot", section, primary);

            PdfPTable lotTable = new PdfPTable(2);
            lotTable.setWidthPercentage(100);
            lotTable.setSpacingAfter(12f);
            lotTable.setWidths(new float[] { 1.3f, 2.7f });
            addLabeledCell(lotTable, "Variete", nullSafe(lot.getVariete()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Maturite", nullSafe(lot.getMaturite()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Origine", nullSafe(lot.getOrigine()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Region", nullSafe(lot.getRegion()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Methode recolte", nullSafe(lot.getMethodeRecolte()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Type sol", nullSafe(lot.getTypeSol()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Temps depuis recolte",
                    (lot.getTempsDepuisRecolteHeures() == null ? "-" : lot.getTempsDepuisRecolteHeures() + " heure(s)"),
                    label, value, labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Date recolte", nullSafe(lot.getDateRecolte()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Duree avant broyage", (lot.getDureeStockageAvantBroyage() == null ? "-"
                            : lot.getDureeStockageAvantBroyage() + " jour(s)"), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(lotTable, "Pesee nette",
                    String.format(java.util.Locale.US, "%.2f kg", lot.getPesee() == null ? 0d : lot.getPesee()), label,
                    value, labelBackground, valueBackground, border);
            document.add(lotTable);

            addSectionBanner(document, "Fournisseur", section, primary);

            PdfPTable supplierTable = new PdfPTable(2);
            supplierTable.setWidthPercentage(100);
            supplierTable.setWidths(new float[] { 1.3f, 2.7f });
            addLabeledCell(supplierTable, "Nom", nullSafe(lot.getFournisseurNom()), label, value,
                    labelBackground, valueBackground, border);
            addLabeledCell(supplierTable, "CIN", nullSafe(lot.getFournisseurCIN()), label, value,
                    labelBackground, valueBackground, border);
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

    private void addSectionBanner(Document document, String title, Font titleFont, Color backgroundColor)
            throws com.lowagie.text.DocumentException {
        PdfPTable titleTable = new PdfPTable(1);
        titleTable.setWidthPercentage(100);
        titleTable.setSpacingBefore(4f);
        titleTable.setSpacingAfter(6f);

        PdfPCell titleCell = new PdfPCell(new Phrase(title, titleFont));
        titleCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        titleCell.setBackgroundColor(backgroundColor);
        titleCell.setPadding(8f);
        titleCell.setBorderColor(backgroundColor);
        titleTable.addCell(titleCell);

        document.add(titleTable);
    }

    private void addLabeledCell(PdfPTable table, String fieldLabel, String fieldValue, Font labelFont, Font valueFont,
                                Color labelBackground, Color valueBackground, Color borderColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(fieldLabel, labelFont));
        labelCell.setBackgroundColor(labelBackground);
        labelCell.setPadding(8f);
        labelCell.setBorderColor(borderColor);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(fieldValue, valueFont));
        valueCell.setBackgroundColor(valueBackground);
        valueCell.setPadding(8f);
        valueCell.setBorderColor(borderColor);
        table.addCell(valueCell);
    }

    private Color mix(Color base, Color mixWith, float mixRatio) {
        float ratio = Math.max(0f, Math.min(1f, mixRatio));
        int red = Math.round(base.getRed() * (1f - ratio) + mixWith.getRed() * ratio);
        int green = Math.round(base.getGreen() * (1f - ratio) + mixWith.getGreen() * ratio);
        int blue = Math.round(base.getBlue() * (1f - ratio) + mixWith.getBlue() * ratio);
        return new Color(red, green, blue);
    }

    private String nullSafe(String value) {
        return value == null ? "-" : value;
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}