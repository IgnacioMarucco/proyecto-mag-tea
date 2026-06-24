package com.utn.magtea.paciente.cars;

import com.utn.magtea.common.exception.BusinessRuleException;
import com.utn.magtea.paciente.Paciente;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CarsService {

    private static final List<BigDecimal> VALORES_VALIDOS = List.of(
            new BigDecimal("1.0"), new BigDecimal("1.5"), new BigDecimal("2.0"),
            new BigDecimal("2.5"), new BigDecimal("3.0"), new BigDecimal("3.5"),
            new BigDecimal("4.0"));

    /** Valida los 15 ítems. Lanza BusinessRuleException si alguno es inválido. */
    public void validarItems(CarsDTO dto) {
        BigDecimal[] items = {
            dto.item1(), dto.item2(), dto.item3(), dto.item4(), dto.item5(),
            dto.item6(), dto.item7(), dto.item8(), dto.item9(), dto.item10(),
            dto.item11(), dto.item12(), dto.item13(), dto.item14(), dto.item15()
        };
        for (int i = 0; i < items.length; i++) {
            BigDecimal item = items[i];
            if (item == null || VALORES_VALIDOS.stream().noneMatch(v -> v.compareTo(item) == 0)) {
                throw new BusinessRuleException(
                        "Valor inválido en ítem " + (i + 1) + " de CARS-2: " + items[i]
                        + ". Valores permitidos: 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0");
            }
        }
    }

    /** Crea o actualiza EvaluacionCars en el paciente (sin guardar — el llamador guarda). */
    public void aplicar(Paciente paciente, CarsDTO dto) {
        BigDecimal rawScore = dto.item1().add(dto.item2()).add(dto.item3()).add(dto.item4()).add(dto.item5())
                .add(dto.item6()).add(dto.item7()).add(dto.item8()).add(dto.item9()).add(dto.item10())
                .add(dto.item11()).add(dto.item12()).add(dto.item13()).add(dto.item14()).add(dto.item15());

        EvaluacionCars cars = Optional.ofNullable(paciente.getEvaluacionCars())
                .orElseGet(() -> { var c = new EvaluacionCars(); c.setPaciente(paciente); return c; });
        cars.setItem1(dto.item1());   cars.setItem2(dto.item2());   cars.setItem3(dto.item3());
        cars.setItem4(dto.item4());   cars.setItem5(dto.item5());   cars.setItem6(dto.item6());
        cars.setItem7(dto.item7());   cars.setItem8(dto.item8());   cars.setItem9(dto.item9());
        cars.setItem10(dto.item10()); cars.setItem11(dto.item11()); cars.setItem12(dto.item12());
        cars.setItem13(dto.item13()); cars.setItem14(dto.item14()); cars.setItem15(dto.item15());
        cars.setObs1(dto.obs1());     cars.setObs2(dto.obs2());     cars.setObs3(dto.obs3());
        cars.setObs4(dto.obs4());     cars.setObs5(dto.obs5());     cars.setObs6(dto.obs6());
        cars.setObs7(dto.obs7());     cars.setObs8(dto.obs8());     cars.setObs9(dto.obs9());
        cars.setObs10(dto.obs10());   cars.setObs11(dto.obs11());   cars.setObs12(dto.obs12());
        cars.setObs13(dto.obs13());   cars.setObs14(dto.obs14());   cars.setObs15(dto.obs15());
        cars.setRawScore(rawScore);
        cars.setTScore(dto.tScore());
        cars.setPercentil(dto.percentil());
        paciente.setEvaluacionCars(cars);
    }
}
