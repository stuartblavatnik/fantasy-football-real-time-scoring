package com.twoforboth.realtimescoring.data;

import com.twoforboth.communication.ServletInfo;
import com.twoforboth.realtimescoring.data.NFLPlayer;
import com.twoforboth.realtimescoring.data.NFLTeams;

import java.util.ArrayList;
import java.util.StringTokenizer;
/**
 * <p>Title: FantasyTeam.kava</p>
 * <p>Description: Fantasy Team data object.  A fantasy team represents one of<BR>
 * the teams within a fantasy league.  This object contains a name, number, <BR>
 * opponent's number, current score and an array of players playing for the <BR>
 * week.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class FantasyTeam
{
  private final static String LINEUP_INNER_DELIMITER = "`";

  private String name_ = "";                       //Name of team
  private int number_ = 0;                         //Team number
  private int score_ = 0;                          //Current score
  private int opponent_ = 0;                       //Opponent's team number
  private ArrayList weeklyLineup_ = null;          //Will hold the list of players for the week

  //Tokenizers for filling the team's lineup
  private StringTokenizer st_ = null;
  private StringTokenizer ist_ = null;

  /**
   * Constructor initializes the name and number as well as creating the array<BR>
   * to hold the lineup for the week
   * @param name Team name
   * @param number Team number
   */
  public FantasyTeam(String name, int number)
  {
    name_ = name;
    number_ = number;
    weeklyLineup_ = new ArrayList();
  }

  //Getters
  public String getName() { return name_; }
  public int getNumber() { return number_; }
  public int getOpponent() { return opponent_; }
  public ArrayList getWeeklyLineup() { return weeklyLineup_; }
  public int getScore() { return score_; }

  //Setters
  public void setScore(int score) { score_ = score; }
  public void setOpponent(int opponent) { opponent_ = opponent; }

  /**
   * Adds to the weekly score
   * @param score New points to add to score
   */

  public void addScore(int score) { score_ += score; }

  public void addPlayer(NFLPlayer nflPlayer)
  {
    weeklyLineup_.add(nflPlayer);
  }

  /**
   * Populates the weekly array lineup from a string in the form:<BR><BR>
   * Banks, Tony`HOU`QB~Alexander, Curtis`MIA`RB
   * @param lineup String containing delimited lineup
   * @param nflTeams Collection of NFLTeam objects
   */

  public void fillLineup(String lineup, NFLTeams nflTeams)
  {
    String name;
    String team;
    String position;
    NFLTeam nt = null;
    boolean found = false;

    //Parse the outer portion of the string (i.e. get Banks, Tony~HOU`QB
    st_ = new StringTokenizer(lineup, ServletInfo.DELIMITER, false);
    while (st_.hasMoreElements())
    {
      //Break up the string by the ` character
      ist_ = new StringTokenizer(st_.nextToken(), LINEUP_INNER_DELIMITER, false);

      name = ist_.nextToken();               //Banks, Tony
      team = ist_.nextToken();               //HOU
      position = ist_.nextToken();           //QB

      //Find the matching team name (figure out why I need this)
      nflTeams.getFirst();
      while (nflTeams.hasNext())
      {
	nt = nflTeams.next();
	if (nt.getShortName().equals(team))
	{
	  found = true;
	  break;
	}
      }
      //If a team was found, create and add the player to the lineup array
      if (found)
      {
	NFLPlayer np = new NFLPlayer(name, nt.getShortName(), position);
	addPlayer(np);
      }
    }
  }
}