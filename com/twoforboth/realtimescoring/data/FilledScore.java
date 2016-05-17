package com.twoforboth.realtimescoring.data;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class FilledScore
{
  private String fantasyTeamName_ = "";
  private String playerName_ = "";
  private String playerNameNoInitial_ = "";
  private String nflTeam_ = "";
  private String nflTeamAlternativeName_ = "";
  private String position_ = "";
  private int points_ = 0;

  public FilledScore(String fantasyTeamName,
		     String playerName,
		     String playerNameNoInitial,
		     String nflTeamName,
		     String nflTeamAlternativeName,
		     String position)
  {
    fantasyTeamName_ = fantasyTeamName;
    playerName_ = playerName;
    playerNameNoInitial_ = playerNameNoInitial;
    nflTeam_ = nflTeamName;
    nflTeamAlternativeName_ = nflTeamAlternativeName;
    position_ = position;
/*
    System.out.println("FilledScore constructor playerName = " + playerName_ +
		       " playerNameNoInitial = " + playerNameNoInitial_ +
		       " nflTeamName = " + nflTeam_ +
		       " nflTeamAlternativeName = " + nflTeamAlternativeName_ +
		       " position = " + position_);
*/
  }

  public void setFantasyTeamName(String name) { fantasyTeamName_ = name; }
  public void setPlayerName(String name) { playerName_ = name; }
  public void setPlayerNameNoInitial(String name) { playerNameNoInitial_ = name; }
  public void setNFLTeam(String name) { nflTeam_ = name; }
  public void setAlternativeNFLTeam(String name) { nflTeamAlternativeName_ = name; }
  public void setPosition(String position) { position_ = position; }
  public void setPoints(int points)  { points_ = points; }

  public String getFantasyTeamName() { return fantasyTeamName_; }
  public String getPlayerName() { return playerName_; }
  public String getPlayerNameNoInitial() { return playerNameNoInitial_; }
  public String getNFLTeam() { return nflTeam_; }
  public String getAlternativeNFLTeam() { return nflTeamAlternativeName_; }
  public String getPosition() { return position_; }
  public int getPoints() { return points_; }

  public void addPoints(int points) { points_ += points; }

}