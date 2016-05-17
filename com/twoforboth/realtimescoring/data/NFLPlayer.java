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

public class NFLPlayer
{
  private String name_ = "";
  private String nflTeam_ = "";
  private String position_ = "";
  private int points_ = 0;
  private Date lastUpdateTime_ = new Date(0);

  public NFLPlayer(String name, String nflTeam, String position)
  {
    name_ = name;
    nflTeam_ = nflTeam;
    position_ = position;
  }

  public String getName() { return name_; }
  public String getNFLTeam() { return nflTeam_; }
  public int getPoints() { return points_; }
  public Date getLastUpdateTime() { return lastUpdateTime_; }
  public String getPosition() { return position_; }

  public void setPoints(int points) { points_ = points; }
  public void setUpdateTime() { lastUpdateTime_ = new Date(); }

  public void addPoints(int points) { points_ += points; }

  public static String fixName(String originalName)
  {
    return originalName.replace('\'', ' ');
  }
}