package org.example.accountreservation.specification;

import org.example.accountreservation.entity.Client;
import org.springframework.data.jpa.domain.Specification;

public final class ClientSpecifications {

    private ClientSpecifications() {
    }

    public static Specification<Client> byFilters(String lastName, Long mdmId) {
        return lastNameEquals(lastName).and(mdmIdEquals(mdmId));
    }

    private static Specification<Client> lastNameEquals(String lastName) {
        return (root, query, criteriaBuilder) -> {
            if (lastName == null || lastName.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("lastName"), lastName);
        };
    }

    private static Specification<Client> mdmIdEquals(Long mdmId) {
        return (root, query, criteriaBuilder) -> {
            if (mdmId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("mdmId"), mdmId);
        };
    }
}
