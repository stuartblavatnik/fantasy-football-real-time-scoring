package com.twoforboth.realtimescoring.data;

import java.util.Date;
/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class NFLGame
{
  private int homeNumber_ = 0;
  private int visitorNumber_ = 0;
  private long gameDate_ = 0;
  private boolean finished_ = false;
  private boolean started_ = false;
  private Date lastTimeUpdated_ = new Date();

  private String visitingTeamShortName_ = "";
  private String visitingTeamLongName_ = "";
  private String homeTeamShortName_ = "";
  private String homeTeamLongName_ = "";

  public NFLGame(int homeNumber, int visitorNumber, long gameDate)
  {
    homeNumber_ = homeNumber;
    visitorNumber_ = visitorNumber;
    gameDate_ = gameDate;
  }

  public int getHomeNumber() { return homeNumber_; }
  public int getVisitorNumber() { return visitorNumber_; }
  public long getGameDate() { return gameDate_; }
  public boolean getFinished() { return finished_; }
  public boolean getStarted() { return started_; }
  public Date getLastTimeUpdated() { return lastTimeUpdated_; }

  public void setFinished(boolean finished) { finished_ = finished; }
  public void setStarted(boolean started) { started_ = started; }
  public void setLastTimeUpdated(Date lastTimeUpdated) { lastTimeUpdated_ = lastTimeUpdated; }

  public void setVisitingTeamShortName(String name) { visitingTeamShortName_ = name; }
  public void setVisitingTeamLongName(String name) { visitingTeamLongName_ = name; }
  public void setHomeTeamShortName(String name) { homeTeamShortName_ = name; }
  public void setHomeTeamLongName(String name) { homeTeamLongName_ = name; }

  public String getVisitingTeamShortName() { return visitingTeamShortName_; }
  public String getVisitingTeamLongName() { return visitingTeamLongName_; }
  public String getHomeTeamShortName() { return homeTeamShortName_; }
  public String getHomeTeamLongName() { return homeTeamLongName_; }
}