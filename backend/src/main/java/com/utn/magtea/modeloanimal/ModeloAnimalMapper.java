package com.utn.magtea.modeloanimal;

import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {VusBanda.class, SocializacionResultado.class})
public interface ModeloAnimalMapper {

    @Mapping(target = "poolId", source = "modeloAnimal.pool.id")
    @Mapping(target = "poolRango", source = "modeloAnimal.pool.rango")
    @Mapping(target = "camadaNombre", source = "modeloAnimal.camada.nombre")
    @Mapping(target = "necesitaVocalizaciones", source = "necesitaVocalizaciones")
    @Mapping(target = "necesitaTresCamaras", source = "necesitaTresCamaras")
    ModeloAnimalListDTO toListDTO(ModeloAnimal modeloAnimal, boolean necesitaVocalizaciones, boolean necesitaTresCamaras);

    @Mapping(target = "poolId", source = "modeloAnimal.pool.id")
    @Mapping(target = "poolRango", source = "modeloAnimal.pool.rango")
    @Mapping(target = "camadaId", source = "modeloAnimal.camada.id")
    @Mapping(target = "camadaNombre", source = "modeloAnimal.camada.nombre")
    @Mapping(target = "necesitaVocalizaciones", source = "necesitaVocalizaciones")
    @Mapping(target = "necesitaTresCamaras", source = "necesitaTresCamaras")
    @Mapping(target = "vocalizaciones", source = "vocalizacionesDTO")
    @Mapping(target = "tresCamaras", source = "tresCamarasDTO")
    ModeloAnimalResponseDTO toDTO(ModeloAnimal modeloAnimal,
                                  boolean necesitaVocalizaciones,
                                  boolean necesitaTresCamaras,
                                  VocalizacionesDTO vocalizacionesDTO,
                                  TresCamarasDTO tresCamarasDTO);

    @Mapping(target = "vusBanda1",
             expression = "java(vus.getMuestra1Khz() != null ? (vus.getMuestra1Khz() >= 20 && vus.getMuestra1Khz() <= 50 ? VusBanda.AVERSIVA : VusBanda.APETITIVA) : null)")
    @Mapping(target = "vusBanda2",
             expression = "java(vus.getMuestra2Khz() != null ? (vus.getMuestra2Khz() >= 20 && vus.getMuestra2Khz() <= 50 ? VusBanda.AVERSIVA : VusBanda.APETITIVA) : null)")
    VocalizacionesDTO toVocalizacionesDTO(VocalizacionesUltrasonicas vus);

    @Mapping(target = "sociabilizacion1",
             expression = "java(tc.getM1TiempoRatonNovedad() / tc.getM1TiempoObjetoNovedoso() <= 1 ? SocializacionResultado.FALTA_SOCIALIZACION : SocializacionResultado.NORMAL)")
    @Mapping(target = "sociabilizacion2",
             expression = "java(tc.getM2TiempoRatonDesconocido() / tc.getM2TiempoRatonFamiliar() <= 1 ? SocializacionResultado.FALTA_SOCIALIZACION : SocializacionResultado.NORMAL)")
    TresCamarasDTO toTresCamarasDTO(TresCamaras tc);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "pool", ignore = true)
    @Mapping(target = "camada", ignore = true)
    @Mapping(target = "vocalizaciones", ignore = true)
    @Mapping(target = "tresCamaras", ignore = true)
    @Mapping(target = "numCelulasGanglionares", ignore = true)
    @Mapping(target = "numCelulasPurkinje", ignore = true)
    ModeloAnimal toEntity(ModeloAnimalCreateDTO dto);
}
