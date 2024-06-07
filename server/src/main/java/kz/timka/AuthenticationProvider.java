package kz.timka;

public interface AuthenticationProvider {

    String getUsernameByLoginAndPassword(String login, String password);

    void changeList(String currentNickname, String newNickname);
}
