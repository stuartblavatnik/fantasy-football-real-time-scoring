package com.twoforboth.realtimescoring.data;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class Score
{
  private String name_ = "";
  private String nflTeam_ = "";
  private int type_ = 0;
  private int length_ = 0;
  private int worth_ = 0;

  /**
   * Scoring Rules done outside
   */
  public Score(String name,
	       String nflTeam,
	       int type,
	       int length)
  {
//    System.out.println("Score constructor name = <" + name + "> nflTeam = <" + nflTeam + "> type = " + type + " length = " + length);

    name_ = name;
    nflTeam_ = nflTeam;
    type_ = type;
    length_ = length;
  }

  public String getName() { return name_; }
  public String getNFLTeam() { return nflTeam_; }
  public int getType() { return type_; }
  public int getLength() { return length_; }
  public int getWorth() { return worth_; }

  public void setWorth(int worth) { worth_ = worth; }

}