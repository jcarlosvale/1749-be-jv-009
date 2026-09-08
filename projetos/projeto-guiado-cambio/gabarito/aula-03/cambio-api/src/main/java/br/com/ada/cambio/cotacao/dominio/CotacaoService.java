package br.com.ada.cambio.cotacao.dominio;

import br.com.ada.cambio.cotacao.infra.CotacaoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Consulta e atualização da cotação vigente.
 *
 * <p>Na Aula 3 a origem do dado é a tabela local, e o serviço fala com o repositório
 * direto. Na Aula 4 essa dependência vira um <b>contrato</b> ({@code CotacaoProvider}),
 * para que a mesma API funcione com cotação local ou com o provedor externo.</p>
 */
@Service
public class CotacaoService {

    private final CotacaoRepository repositorio;

    public CotacaoService(CotacaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Cotação vigente da moeda.
     *
     * @throws MoedaNaoSuportadaException se não houver cotação registrada para a moeda
     */
    @Transactional(readOnly = true)
    public Cotacao consultar(Moeda moeda) {
        return repositorio.findByMoeda(moeda)
                .orElseThrow(() -> new MoedaNaoSuportadaException(String.valueOf(moeda)));
    }

    /** Mesma consulta, a partir da sigla recebida na URL. Sigla desconhecida → 422. */
    @Transactional(readOnly = true)
    public Cotacao consultarPorSigla(String sigla) {
        return consultar(Moeda.paraSigla(sigla));
    }

    /**
     * Atualiza a cotação da tabela local (simulação de variação de mercado).
     *
     * <p>É o que permite demonstrar, sem rede externa, que uma ordem criada <b>depois</b> da
     * atualização sai com outro valor total — a cotação é gravada no momento da ordem, não
     * lida de novo depois.</p>
     *
     * @throws MoedaNaoSuportadaException se a moeda não é operada
     */
    @Transactional
    public Cotacao atualizar(String sigla, BigDecimal novoValor) {
        Moeda moeda = Moeda.paraSigla(sigla);
        Cotacao cotacao = repositorio.findByMoeda(moeda)
                .orElseThrow(() -> new MoedaNaoSuportadaException(sigla));
        cotacao.atualizar(novoValor.setScale(Cotacao.ESCALA_COTACAO, RoundingMode.HALF_EVEN),
                LocalDateTime.now());
        return repositorio.save(cotacao);
    }
}
