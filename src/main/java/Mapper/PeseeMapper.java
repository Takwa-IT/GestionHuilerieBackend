package Mapper;

import Models.Pesee;
import Models.Huilerie;
import Models.Stock;
import dto.PeseeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashSet;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface PeseeMapper {

    @Mapping(target = "idPesee", source = "id")
    @Mapping(target = "lotId", source = "lot.idLot")
    @Mapping(target = "huilerieId", expression = "java(resolveHuilerieId(entity))")
    @Mapping(target = "huilerieNom", expression = "java(resolveHuilerieNom(entity))")
    PeseeDTO toDTO(Pesee entity);

    default Long resolveHuilerieId(Pesee entity) {
        Huilerie huilerie = resolveHuilerie(entity);
        return huilerie != null ? huilerie.getIdHuilerie() : null;
    }

    default String resolveHuilerieNom(Pesee entity) {
        Huilerie huilerie = resolveHuilerie(entity);
        return huilerie != null ? huilerie.getNom() : null;
    }

    private Huilerie resolveHuilerie(Pesee entity) {
        if (entity == null || entity.getLot() == null || entity.getLot().getStocks() == null) {
            return null;
        }

        Set<Long> huilerieIds = entity.getLot().getStocks().stream()
                .map(Stock::getHuilerie)
                .filter(huilerie -> huilerie != null && huilerie.getIdHuilerie() != null)
                .map(Huilerie::getIdHuilerie)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));

        if (huilerieIds.size() == 1) {
            Long huilerieId = huilerieIds.iterator().next();
            return entity.getLot().getStocks().stream()
                    .map(Stock::getHuilerie)
                    .filter(huilerie -> huilerie != null && huilerieId.equals(huilerie.getIdHuilerie()))
                    .findFirst()
                    .orElse(null);
        }

        return null;
    }
}
