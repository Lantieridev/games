package ar.edu.utn.frc.tup.piii;


public class Player {

    private String name;
    private Integer chips;
    private Boolean isBot;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getChips() {
        return chips;
    }

    public void setChips(Integer chips) {
        this.chips = chips;
    }

    public Boolean getBot() {
        return isBot;
    }

    public void setBot(Boolean bot) {
        isBot = bot;
    }

    public Player() {
    }

    public Player(String playerName, Integer chips, Boolean isBot) {
        this.name = playerName;
        this.chips = chips;
        this.isBot = isBot;
    }
}
