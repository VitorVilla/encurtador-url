package com.topaz.encurtador.service;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ShortCodeGeneratorTest {

    private static final Pattern BASE62 = Pattern.compile("^[A-Za-z0-9]+$");

    private final ShortCodeGenerator generator = new ShortCodeGenerator();

    @Test
    public void generate_usaTamanhoPadraoDe6() {
        assertEquals(6, generator.generate().length());
    }

    @Test
    public void generate_respeitaTamanhoCustomizado() {
        assertEquals(10, generator.generate(10).length());
    }

    @Test
    public void generate_produzApenasCaracteresBase62() {
        for (int i = 0; i < 1000; i++) {
            String code = generator.generate();
            assertTrue("codigo fora do Base62: " + code, BASE62.matcher(code).matches());
        }
    }

    @Test
    public void generate_produzValoresVariados() {
        Set<String> gerados = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            gerados.add(generator.generate());
        }
        assertTrue("baixa variacao de codigos gerados", gerados.size() > 990);
    }
}
