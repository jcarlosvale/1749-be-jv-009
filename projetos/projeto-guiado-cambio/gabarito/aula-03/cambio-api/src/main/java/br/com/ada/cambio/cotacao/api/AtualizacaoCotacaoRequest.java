package br.com.ada.cambio.cotacao.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/** Corpo do {@code PUT /cotacoes/{moeda}}: o novo valor da cotação local. */
public record AtualizacaoCotacaoRequest(

        @NotNull(message = "valorCotacao é obrigatório")
        @Positive(message = "valorCotacao deve ser maior que zero")
        BigDecimal valorCotacao) {
}
