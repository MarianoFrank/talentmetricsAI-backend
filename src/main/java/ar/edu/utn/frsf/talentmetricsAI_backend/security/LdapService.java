package ar.edu.utn.frsf.talentmetricsAI_backend.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.directory.Attributes;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

@Service
public class LdapService {

    private final LdapTemplate ldapTemplate;

    public LdapService(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    // Método para validar si el usuario sigue existiendo (para el /refresh)
    public boolean isUserActive(String username) {
        try {
            LdapQuery query = LdapQueryBuilder.query().where("uid").is(username);
            return !ldapTemplate.search(query, (Attributes attributes) -> attributes.get("uid")).isEmpty();
        } catch (Exception e) {
            System.out.println("Error verificando usuario en LDAP: " + e.getMessage());
            return false;
        }
    }

    // Método para traer el perfil completo (para el /me)
    public Map<String, Object> getUserProfile(String username) {
        try {
            LdapQuery query = LdapQueryBuilder.query().where("uid").is(username);

            List<Map<String, Object>> ldapResults = ldapTemplate.search(query, (Attributes attributes) -> {
                Map<String, Object> userData = new HashMap<>();
                if (attributes.get("cn") != null)
                    userData.put("name", attributes.get("cn").get().toString());
                if (attributes.get("sn") != null)
                    userData.put("lastname", attributes.get("sn").get().toString());
                if (attributes.get("employeeNumber") != null)
                    userData.put("legajo", attributes.get("employeeNumber").get().toString());
                return userData;
            });

            return ldapResults.isEmpty() ? null : ldapResults.get(0);

        } catch (Exception e) {
            System.out.println("Error obteniendo perfil de LDAP: " + e.getMessage());
            return null;
        }
    }
}
