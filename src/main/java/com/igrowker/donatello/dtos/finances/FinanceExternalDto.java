package com.igrowker.donatello.dtos.finances;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceExternalDto {

    private Integer id_finanza;

    private String tipo;

    private Double monto;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fecha;

    private Integer id_usuario;

    private String descripcion;

    private FinanceOrigin motivo;
}
