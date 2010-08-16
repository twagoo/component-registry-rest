package clarin.cmdi.componentregistry.rest;

import java.security.Principal;

import clarin.cmdi.componentregistry.UserCredentials;

public final class DummyPrincipal implements Principal {
    public static final DummyPrincipal DUMMY_PRINCIPAL = new DummyPrincipal("JUnit@test.com");
    public static final UserCredentials DUMMY_CREDENTIALS = new UserCredentials(DUMMY_PRINCIPAL) {
        @Override
        public String getDisplayName() {
            return "J.Unit";
        }
    };

    private final String username;

    public DummyPrincipal(String username) {
        this.username = username;
    }

    public String getName() {
        return username;
    }
}