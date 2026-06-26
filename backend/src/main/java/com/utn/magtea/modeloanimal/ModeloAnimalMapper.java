package com.utn.magtea.modeloanimal;

import com.utn.magtea.modeloanimal.estudios.TresCamaras;
import com.utn.magtea.modeloanimal.estudios.TresCamarasDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesDTO;
import com.utn.magtea.modeloanimal.estudios.VocalizacionesUltrasonicas;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", imports = {VusBanda.class, SocializacionResultado.class})
public interface ModeloAnimalMapper {

    @Mapping(target = "poolId",          source = "modeloAnimal.pool.id")
    @Mapping(target = "poolRango",       source = "modeloAnimal.pool.rango")
    @Mapping(target = "poolCodigo",      source = "modeloAnimal.pool.codigo")
    @Mapping(target = "poolUso",         source = "modeloAnimal.pool.uso")
    @Mapping(target = "camadaNombre",    source = "modeloAnimal.camada.nombre")
    @Mapping(target = "fechaNacimiento", source = "modeloAnimal.camada.fechaNacimiento")
    @Mapping(target = "aportesCount",    expression = "java(modeloAnimal.getAportes().size())")
    @Mapping(target = "necesitaVocalizaciones", source = "necesitaVocalizaciones")
    @Mapping(target = "necesitaTresCamaras",    source = "necesitaTresCamaras")
    ModeloAnimalListDTO toListDTO(ModeloAnimal modeloAnimal, boolean necesitaVocalizaciones, boolean necesitaTresCamaras);

    @Mapping(target = "poolId",          source = "modeloAnimal.pool.id")
    @Mapping(target = "poolRango",       source = "modeloAnimal.pool.rango")
    @Mapping(target = "poolCodigo",      source = "modeloAnimal.pool.codigo")
    @Mapping(target = "poolUso",         source = "modeloAnimal.pool.uso")
    @Mapping(target = "camadaId",        source = "modeloAnimal.camada.id")
    @Mapping(target = "camadaNombre",    source = "modeloAnimal.camada.nombre")
    @Mapping(target = "fechaNacimiento", source = "modeloAnimal.camada.fechaNacimiento")
    @Mapping(target = "necesitaVocalizaciones", source = "necesitaVocalizaciones")
    @Mapping(target = "necesitaTresCamaras",    source = "necesitaTresCamaras")
    @Mapping(target = "vocalizaciones", source = "vocalizacionesDTO")
    @Mapping(target = "tresCamaras",    source = "tresCamarasDTO")
    @Mapping(target = "aportes",              source = "modeloAnimal.aportes")
    @Mapping(target = "imagenesMicroscopia",  source = "modeloAnimal.imagenesMicroscopia")
    ModeloAnimalResponseDTO toDTO(ModeloAnimal modeloAnimal,
                                  boolean necesitaVocalizaciones,
                                  boolean necesitaTresCamaras,
                                  VocalizacionesDTO vocalizacionesDTO,
                                  TresCamarasDTO tresCamarasDTO);

    @Mapping(target = "vusBanda1", expression = "java(VusBanda.from(vus.getMuestra1Khz()))")
    @Mapping(target = "vusBanda2", expression = "java(VusBanda.from(vus.getMuestra2Khz()))")
    VocalizacionesDTO toVocalizacionesDTO(VocalizacionesUltrasonicas vus);

    @Mapping(target = "sociabilizacion1", expression = "java(SocializacionResultado.from(tc.getM1TiempoRatonNovedad(), tc.getM1TiempoObjetoNovedoso()))")
    @Mapping(target = "sociabilizacion2", expression = "java(SocializacionResultado.from(tc.getM2TiempoRatonDesconocido(), tc.getM2TiempoRatonFamiliar()))")
    TresCamarasDTO toTresCamarasDTO(TresCamaras tc);

    @Mapping(target = "posicion", source = "tubo.posicion")
    @Mapping(target = "poolTuboId", source = "tubo.id")
    ModeloAnimalPoolAporteDTO toAporteDTO(ModeloAnimalPoolAporte aporte);

    @Mapping(target = "documentoId", source = "imagen.documento.id")
    ImagenMicroscopiaDTO toImagenDTO(ImagenMicroscopia imagen);

    @Mapping(target = "id",                     ignore = true)
    @Mapping(target = "activo",                 ignore = true)
    @Mapping(target = "identificador",          ignore = true)
    @Mapping(target = "pool",                   ignore = true)
    @Mapping(target = "camada",                 ignore = true)
    @Mapping(target = "fechaDia1Inoculacion",   ignore = true)
    @Mapping(target = "vocalizaciones",         ignore = true)
    @Mapping(target = "tresCamaras",            ignore = true)
    @Mapping(target = "aportes",                ignore = true)
    @Mapping(target = "imagenesMicroscopia",    ignore = true)
    @Mapping(target = "numCelulasGanglionares", ignore = true)
    @Mapping(target = "numCelulasPurkinje",     ignore = true)
    @Mapping(target = "estadoProtocolo",        ignore = true)
    @Mapping(target = "fechaProximoEvento",     ignore = true)
    ModeloAnimal toEntity(ModeloAnimalCreateDTO dto);
}
