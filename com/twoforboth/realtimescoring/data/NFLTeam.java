package com.twoforboth.realtimescoring.data;

//import java.util.Date;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class NFLTeam
{
  private String shortName_ = "";
  private String longName_ = "";
  private int number_ = 0;
//  private int opponent_ = 0;
//  private Date gameDate_ = new Date();

  public NFLTeam(int number, String shortName, String longName)
  {
    shortName_ = shortName;
    longName_ = longName;
    number_ = number;
  }

  public String getShortName() { return shortName_; }
  public String getLongName() { return longName_; }
  public int getNumber() { return number_; }
}