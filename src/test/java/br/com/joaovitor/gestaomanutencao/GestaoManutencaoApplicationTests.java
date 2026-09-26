package br.com.joaovitor.gestaomanutencao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "app.jwt.secret=chave-secreta-apenas-para-testes-automatizados-nao-use-em-producao")
class GestaoManutencaoApplicationTests {

    @Test
    void contextLoads() {
    }

}
