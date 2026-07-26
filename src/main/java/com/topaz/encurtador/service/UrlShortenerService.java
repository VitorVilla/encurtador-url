package com.topaz.encurtador.service;

import com.topaz.encurtador.exception.AliasAlreadyInUseException;
import com.topaz.encurtador.exception.InvalidAliasException;
import com.topaz.encurtador.exception.InvalidUrlException;
import com.topaz.encurtador.exception.ShortUrlNotFoundException;
import com.topaz.encurtador.model.ShortUrl;
import com.topaz.encurtador.repository.UrlRepository;

import javax.ejb.AccessTimeout;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class UrlShortenerService {

    private static final int MAX_GENERATION_ATTEMPTS = 5;

    private static final int ALIAS_MIN_LENGTH = 3;
    private static final int ALIAS_MAX_LENGTH = 20;

    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    private static final Set<String> RESERVED_ALIASES =
            new HashSet<>(Arrays.asList("api", "index.jsp"));

    @Inject
    private UrlRepository repository;

    @Inject
    private ShortCodeGenerator codeGenerator;

    @Lock(LockType.WRITE)
    @AccessTimeout(value = 10, unit = TimeUnit.SECONDS)
    public ShortUrl create(String originalUrl, String alias) {
        String url = validateAndNormalizeUrl(originalUrl);

        boolean hasAlias = alias != null && !alias.trim().isEmpty();
        String code = hasAlias
                ? reserveAlias(alias.trim())
                : generateUniqueCode();

        return repository.save(new ShortUrl(url, code));
    }

    @Lock(LockType.READ)
    public ShortUrl resolve(String shortCode) {
        return repository.findByShortCode(shortCode)
                .orElseThrow(() -> new ShortUrlNotFoundException(shortCode));
    }

    private String validateAndNormalizeUrl(String originalUrl) {
        if (originalUrl == null || originalUrl.trim().isEmpty()) {
            throw new InvalidUrlException("A URL nao pode ser vazia.");
        }
        String candidate = originalUrl.trim();
        try {
            URL parsed = new URL(candidate);
            String protocol = parsed.getProtocol();
            if (!"http".equalsIgnoreCase(protocol) && !"https".equalsIgnoreCase(protocol)) {
                throw new InvalidUrlException("A URL deve usar o protocolo http ou https.");
            }
            if (parsed.getHost() == null || parsed.getHost().isEmpty()) {
                throw new InvalidUrlException("A URL informada e invalida (sem host).");
            }
        } catch (MalformedURLException e) {
            throw new InvalidUrlException("A URL informada e invalida: " + candidate);
        }
        return candidate;
    }

    private String reserveAlias(String alias) {
        if (alias.length() < ALIAS_MIN_LENGTH || alias.length() > ALIAS_MAX_LENGTH) {
            throw new InvalidAliasException(
                    "O alias deve ter entre " + ALIAS_MIN_LENGTH
                            + " e " + ALIAS_MAX_LENGTH + " caracteres.");
        }
        if (!ALIAS_PATTERN.matcher(alias).matches()) {
            throw new InvalidAliasException(
                    "O alias so pode conter letras, numeros, hifen e underscore.");
        }
        if (RESERVED_ALIASES.contains(alias.toLowerCase())
                || repository.existsByShortCode(alias)) {
            throw new AliasAlreadyInUseException(alias);
        }
        return alias;
    }

    private String generateUniqueCode() {
        for (int attempt = 0; attempt < MAX_GENERATION_ATTEMPTS; attempt++) {
            String code = codeGenerator.generate();
            if (!repository.existsByShortCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException(
                "Nao foi possivel gerar um codigo unico apos "
                        + MAX_GENERATION_ATTEMPTS + " tentativas.");
    }
}
