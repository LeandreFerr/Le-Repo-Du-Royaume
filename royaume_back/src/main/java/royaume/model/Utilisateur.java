package royaume.model;

public class Utilisateur {

private Integer id;
private String pseudo;
private String email;
private String password;
private boolean online = false;
private boolean admin = false;

private Royaume royaume;
private Partie partieActuelle;


//constructeur


public Utilisateur(){}



//methode metier

public Utilisateur(Integer id, String pseudo, String email, String password, boolean online, boolean admin,
        Royaume royaume, Partie partieActuelle) {
    this.id = id;
    this.pseudo = pseudo;
    this.email = email;
    this.password = password;
    this.online = online;
    this.admin = admin;
    this.royaume = royaume;
    this.partieActuelle = partieActuelle;
}



public boolean seConnecter(String password){

    if (password != null && password.equals(this.password)) {

            online = true;

            return true;
        }

        return false;
}

public void seDeconnecter() {

        online = false;

    }


public void abandonnerPartie() {

    royaume = null;

    }

//getters et setters

public Integer getId() {
    return id;
}

public void setId(Integer id) {
    this.id = id;
}

public String getPseudo() {
    return pseudo;
}

public void setPseudo(String pseudo) {
    this.pseudo = pseudo;
}

public String getEmail() {
    return email;
}

public void setEmail(String email) {
    this.email = email;
}

public String getPassword() {
    return password;
}

public void setPassword(String password) {
    this.password = password;
}

public boolean isOnline() {
    return online;
}

public void setOnline(boolean online) {
    this.online = online;
}

public boolean isAdmin() {
    return admin;
}

public void setAdmin(boolean admin) {
    this.admin = admin;
}

public Royaume getRoyaume() {
    return royaume;
}

public void setRoyaume(Royaume royaume) {
    this.royaume = royaume;
}
   
public Partie getPartieActuelle() {
    return partieActuelle;
}



public void setPartieActuelle(Partie partieActuelle) {
    this.partieActuelle = partieActuelle;
}


// toString


@Override
public String toString() {
    return "Utilisateur [id=" + id + ", pseudo=" + pseudo + ", email=" + email + ", password=" + password + ", online="
            + online + ", admin=" + admin + ", royaume=" + royaume + ", partieActuelle=" + partieActuelle + ", getId()="
            + getId() + ", getPseudo()=" + getPseudo() + ", getEmail()=" + getEmail() + ", getPassword()="
            + getPassword() + ", getClass()=" + getClass() + ", isOnline()=" + isOnline() + ", isAdmin()=" + isAdmin()
            + ", getRoyaume()=" + getRoyaume() + ", getPartieActuelle()=" + getPartieActuelle() + ", hashCode()="
            + hashCode() + ", toString()=" + super.toString() + "]";
}


    
}
