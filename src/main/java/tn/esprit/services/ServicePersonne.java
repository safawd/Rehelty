package tn.esprit.services;

import tn.esprit.interfaces.IServices;
import tn.esprit.models.Personne;
import tn.esprit.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.sql.Types.NULL;

public class ServicePersonne implements IServices<Personne> {
    private Connection cnx;

    public ServicePersonne() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public void add(Personne personne) {
        // Validation de l'email si ce n'est pas un utilisateur Google
        if (!personne.isGoogleUser()) {
            String email = personne.getEmail();
            if (!(email.endsWith("@gmail.com") || email.endsWith("@outlook.com") || email.endsWith("@yahoo.com"))) {
                System.out.println("Email invalide ! Seuls @gmail.com , @outlook.com et @yahoo.com sont autorisés.");
                return;
            }
        }

        String qry;

        if (personne.isGoogleUser()) {
            qry = "INSERT INTO `personne` (`email`, `nom`, `prenom`, `telephone`, `motDePasse`, `pays`, `idType`, `idNumber`, `isGoogleUser`, `googleUserId`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            qry = "INSERT INTO `personne` (`email`, `nom`, `prenom`, `telephone`, `motDePasse`, `pays`, `idType`, `idNumber`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        }

        try {
            PreparedStatement pstm = cnx.prepareStatement(qry, Statement.RETURN_GENERATED_KEYS);
            pstm.setString(1, personne.getEmail());
            pstm.setString(2, personne.getNom());
            pstm.setString(3, personne.getPrenom());

            // Si le numéro de téléphone est vide ou égal à 0, insérer NULL
            if (personne.getTelephone() == 0 || personne.getTelephone()==NULL) {
                pstm.setNull(4, Types.INTEGER);  // Met NULL pour le téléphone
            } else {
                pstm.setInt(4, personne.getTelephone()); // Sinon, insérer la valeur du téléphone
            }

            pstm.setString(5, personne.getMotDePasse());
            pstm.setString(6, personne.getPays());
            pstm.setString(7, personne.getIdType());
            pstm.setString(8, personne.getIdNumber());

            if (personne.isGoogleUser()) {
                pstm.setBoolean(9, true);
                pstm.setString(10, personne.getGoogleUserId());
            }

            pstm.executeUpdate();

            ResultSet rs = pstm.getGeneratedKeys();
            if (rs.next()) {
                personne.setIdPersonne(rs.getInt(1));
                System.out.println("Utilisateur ajouté avec l'ID: " + personne.getIdPersonne());
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de l'utilisateur: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public List<Personne> getAll() {
        List<Personne> personnes = new ArrayList<>();
        String qry = "SELECT * FROM `personne`";
        try {
            Statement stm = cnx.createStatement();
            ResultSet rs = stm.executeQuery(qry);

            while (rs.next()) {
                personnes.add(extractPersonneFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }

        return personnes;
    }

    public void update(Personne personne) {
        String qry;

        if (personne.isGoogleUser()) {
            qry = "UPDATE `personne` SET `email` = ?, `nom` = ?, `prenom` = ?, `telephone` = ?, `motDePasse` = ?, `pays` = ?, `idType` = ?, `idNumber` = ?, `isGoogleUser` = ?, `googleUserId` = ? WHERE `idPersonne` = ?";
        } else {
            qry = "UPDATE `personne` SET `email` = ?, `nom` = ?, `prenom` = ?, `telephone` = ?, `motDePasse` = ?, `pays` = ?, `idType` = ?, `idNumber` = ? WHERE `idPersonne` = ?";
        }

        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, personne.getEmail());
            pstm.setString(2, personne.getNom());
            pstm.setString(3, personne.getPrenom());
            pstm.setInt(4, personne.getTelephone());  // Mettre à jour le téléphone avec un int
            pstm.setString(5, personne.getMotDePasse());
            pstm.setString(6, personne.getPays());
            pstm.setString(7, personne.getIdType());
            pstm.setString(8, personne.getIdNumber());

            if (personne.isGoogleUser()) {
                pstm.setBoolean(9, true);
                pstm.setString(10, personne.getGoogleUserId());
                pstm.setInt(11, personne.getIdPersonne());
            } else {
                pstm.setInt(9, personne.getIdPersonne());
            }

            pstm.executeUpdate();
            System.out.println("Utilisateur mis à jour avec succès: ID " + personne.getIdPersonne());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la mise à jour de l'utilisateur: " + e.getMessage());
        }
    }

    @Override
    public void delete(Personne personne) {
        String qry = "DELETE FROM `personne` WHERE `idPersonne` = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setInt(1, personne.getIdPersonne());
            pstm.executeUpdate();
            System.out.println("Utilisateur supprimé avec succès: ID " + personne.getIdPersonne());
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
        }
    }

    public Personne getByEmailAndPassword(String email, String motDePasse) {
        String qry = "SELECT * FROM personne WHERE email = ? AND motDePasse = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, email);
            pstm.setString(2, motDePasse);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return extractPersonneFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la connexion: " + e.getMessage());
        }
        return null;
    }

    public Personne findByGoogleId(String googleId) {
        String qry = "SELECT * FROM personne WHERE googleUserId = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, googleId);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return extractPersonneFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche par ID Google: " + e.getMessage());
        }
        return null;
    }

    public Personne findByEmail(String email) {
        String qry = "SELECT * FROM personne WHERE email = ?";
        try {
            PreparedStatement pstm = cnx.prepareStatement(qry);
            pstm.setString(1, email);
            ResultSet rs = pstm.executeQuery();

            if (rs.next()) {
                return extractPersonneFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche par email: " + e.getMessage());
        }
        return null;
    }

    public Personne authenticateWithGoogle(String googleId, String email, String nom, String prenom) {
        Personne personne = findByGoogleId(googleId);

        if (personne != null) {
            System.out.println("Utilisateur Google trouvé: " + personne.getIdPersonne());
            return personne;
        }

        personne = findByEmail(email);

        if (personne != null) {
            personne.setGoogleUser(true);
            personne.setGoogleUserId(googleId);
            update(personne);
            System.out.println("Utilisateur existant mis à jour avec ID Google: " + personne.getIdPersonne());
            return personne;
        }

        personne = new Personne();
        personne.setEmail(email);
        personne.setNom(nom);
        personne.setPrenom(prenom);
        personne.setGoogleUser(true);
        personne.setGoogleUserId(googleId);

        personne.setMotDePasse(generateRandomPassword());

        add(personne);
        System.out.println("Nouvel utilisateur Google créé: " + personne.getIdPersonne());

        return personne;
    }

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        StringBuilder sb = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    public List<Personne> search(String query) {
        List<Personne> personnes = new ArrayList<>();
        String sql = "SELECT * FROM personne WHERE LOWER(nom) LIKE ? OR LOWER(prenom) LIKE ? OR LOWER(email) LIKE ?";

        try (PreparedStatement pstm = cnx.prepareStatement(sql)) {
            String formattedQuery = "%" + query.toLowerCase() + "%";
            pstm.setString(1, formattedQuery);
            pstm.setString(2, formattedQuery);
            pstm.setString(3, formattedQuery);

            ResultSet rs = pstm.executeQuery();
            while (rs.next()) {
                personnes.add(extractPersonneFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche : " + e.getMessage());
        }

        return personnes;
    }

    private Personne extractPersonneFromResultSet(ResultSet rs) throws SQLException {
        Personne p = new Personne();
        p.setIdPersonne(rs.getInt("idPersonne"));
        p.setEmail(rs.getString("email"));
        p.setNom(rs.getString("nom"));
        p.setPrenom(rs.getString("prenom"));
        p.setTelephone(rs.getInt("telephone"));
        p.setMotDePasse(rs.getString("motDePasse"));
        p.setPays(rs.getString("pays"));
        p.setIdType(rs.getString("idType"));
        p.setIdNumber(rs.getString("idNumber"));

        try {
            p.setGoogleUser(rs.getBoolean("isGoogleUser"));
            p.setGoogleUserId(rs.getString("googleUserId"));
        } catch (SQLException e) {
            p.setGoogleUser(false);
            p.setGoogleUserId(null);
        }

        return p;
    }

    // Statistiques
    public int getCountRecentlyAdded() {
        String sql = "SELECT COUNT(*) FROM personne WHERE idPersonne > (SELECT MAX(idPersonne) - 10 FROM personne)";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des utilisateurs récemment ajoutés: " + e.getMessage());
        }
        return 0;
    }

    public int getCountRecentlyDeleted() {
        // Pour implémenter cette fonctionnalité, vous auriez besoin d'une table de journalisation
        // Voici une implémentation fictive
        return 0;
    }

    public double getTotalPaymentsAmount() {
        // Pour implémenter cette fonctionnalité, vous auriez besoin d'une table de paiements
        // Voici une implémentation fictive
        return 0.0;
    }

    /**
     * Compte le nombre d'utilisateurs Google
     * @return Le nombre d'utilisateurs Google
     */
    public int getCountGoogleUsers() {
        String sql = "SELECT COUNT(*) FROM personne WHERE isGoogleUser = true";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors du comptage des utilisateurs Google: " + e.getMessage());
        }
        return 0;
    }
}

