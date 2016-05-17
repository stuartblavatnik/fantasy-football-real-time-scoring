package com.twoforboth.realtimescoring.data;

import com.twoforboth.realtimescoring.data.FantasyTeam;

/**
 * <p>Title: FantasyTeamMatchup.java</p>
 * <p>Description: Object that represents the matchup between 2 fantasy teams.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class FantasyTeamMatchup
{
  private FantasyTeam home_ = null;
  private FantasyTeam away_ = null;

  /**
   * Constructor
   * @param home FantasyTeam object
   * @param away FantasyTeam object
   */
  public FantasyTeamMatchup(FantasyTeam home, FantasyTeam away)
  {
    home_ = home;
    away_ = away;
  }

  //Getters
  public FantasyTeam getHome() { return home_; }
  public FantasyTeam getAway() { return away_; }
}