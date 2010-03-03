package clarin.cmdi.componentregistry.rest;

import java.security.Principal;

public final class DummyPrincipal implements Principal {
    public static final DummyPrincipal DUMMY_PRINCIPAL = new DummyPrincipal("J.Unit");

    private final String username;

    public DummyPrincipal(String username) {
        this.username = username;
    }

    public String getName() {
        return username;
    }
}