package api.server.models;


public record UserRow(String username, String passwordHashB64, String passwordSaltB64) {}

