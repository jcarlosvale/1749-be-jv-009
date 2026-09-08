package br.com.ada.cambio.cotacao.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.ada.cambio.cotacao.dominio.Cotacao;
import br.com.ada.cambio.cotacao.dominio.CotacaoService;
import br.com.ada.cambio.cotacao.dominio.Moeda;
import br.com.ada.cambio.cotacao.dominio.MoedaNaoSuportadaException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/** Fatia web da cotação: o caminho feliz e o 422 exigido pelo contrato do módulo. */
@WebMvcTest(CotacaoController.class)
class CotacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CotacaoService servico;

    @Test
    @DisplayName("GET /cotacoes/USD devolve 200 com a cotação vigente")
    void cotacaoConhecidaDevolve200() throws Exception {
        when(servico.consultarPorSigla("USD")).thenReturn(new Cotacao(Moeda.USD,
                new BigDecimal("5.4321"), LocalDateTime.of(2026, 9, 14, 9, 0)));

        mockMvc.perform(get("/cotacoes/USD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moeda").value("USD"))
                .andExpect(jsonPath("$.valorCotacao").value(5.4321));
    }

    @Test
    @DisplayName("PUT /cotacoes/USD atualiza a cotação local e devolve 200")
    void atualizacaoDevolve200() throws Exception {
        when(servico.atualizar("USD", new BigDecimal("5.9000"))).thenReturn(new Cotacao(Moeda.USD,
                new BigDecimal("5.9000"), LocalDateTime.of(2026, 9, 14, 10, 0)));

        mockMvc.perform(put("/cotacoes/USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorCotacao\": 5.9000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorCotacao").value(5.9000));
    }

    @Test
    @DisplayName("PUT /cotacoes/USD com valor negativo devolve 400")
    void atualizacaoInvalidaDevolve400() throws Exception {
        mockMvc.perform(put("/cotacoes/USD")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"valorCotacao\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erros[0].campo").value("valorCotacao"));
    }

    @Test
    @DisplayName("GET /cotacoes/JPY devolve 422 — e não 400")
    void moedaNaoOperadaDevolve422() throws Exception {
        when(servico.consultarPorSigla("JPY")).thenThrow(new MoedaNaoSuportadaException("JPY"));

        mockMvc.perform(get("/cotacoes/JPY"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }
}
