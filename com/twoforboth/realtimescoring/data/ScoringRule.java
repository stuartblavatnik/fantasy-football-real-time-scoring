package com.twoforboth.realtimescoring.data;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class ScoringRule
{
  //ID's
  public static final int PLAYER_WEEKLY_PASSING_COMPLETIONS = 2;
  public static final int PLAYER_WEEKLY_PASSING_ATTEMPTS = 3;
  public static final int PLAYER_WEEKLY_PASSING_YDS = 4;
  public static final int PLAYER_WEEKLY_PASSING_INT = 5;
  public static final int PLAYER_WEEKLY_PASSING_TDS = 6;
  public static final int PLAYER_WEEKLY_PASSING_2PT = 7;
  public static final int PLAYER_WEEKLY_SACKED = 8;
  public static final int PLAYER_WEEKLY_SACKED_YDS_LOST = 9;
  public static final int PLAYER_WEEKLY_RUSHING_ATTEMPS = 10;
  public static final int PLAYER_WEEKLY_RUSHING_YDS = 11;
  public static final int PLAYER_WEEKLY_RUSHING_TDS = 12;
  public static final int PLAYER_WEEKLY_RUSHING_2PT = 13;
  public static final int PLAYER_WEEKLY_PASS_RECEPTIONS = 14;
  public static final int PLAYER_WEEKLY_RECEIVING_YDS = 15;
  public static final int PLAYER_WEEKLY_RECEIVING_TDS = 16;
  public static final int PLAYER_WEEKLY_RECEIVING_2PT = 17;
  public static final int PLAYER_WEEKLY_PAT_MADE = 18;
  public static final int PLAYER_WEEKLY_PAT_ATTEMPTED = 19;
  public static final int PLAYER_WEEKLY_FG_MADE = 20;
  public static final int PLAYER_WEEKLY_FG_ATTEMPTED = 21;
  public static final int PLAYER_WEEKLY_FGM_1_29 = 22;
  public static final int PLAYER_WEEKLY_FGM_30_39 = 23;
  public static final int PLAYER_WEEKLY_FGM_40_49 = 24;
  public static final int PLAYER_WEEKLY_FGM_50_UP = 25;
  public static final int PLAYER_WEEKLY_FUMBLES_LOST = 26;

  public static final int SPECIALS_WEEKLY_PUNT_RETURNS = 28;
  public static final int SPECIALS_WEEKLY_PUNT_RETURN_YDS = 29;
  public static final int SPECIALS_WEEKLY_PUNT_RETURN_FAIR_CATCHES = 30;
  public static final int SPECIALS_WEEKLY_PUNT_RETURN_TDS = 31;
  public static final int SPECIALS_WEEKLY_KICKOFF_RETURNS = 32;
  public static final int SPECIALS_WEEKLY_KICKOFF_RETURN_YDS = 33;
  public static final int SPECIALS_WEEKLY_KICKOFF_RETURNS_FAIR_CATCHES = 34;
  public static final int SPECIALS_WEEKLY_KICKOF_RETURN_TDS = 35;
  public static final int SPECIALS_WEEKLY_TOTAL_RETURN_YDS = 36;
  public static final int SPECIALS_WEEKLY_BLOCKED_PUNTS = 37;
  public static final int SPECIALS_WEEKLY_BLOCKED_PATS = 38;
  public static final int SPECIALS_WEEKLY_BLOCKED_FGS = 39;
  public static final int SPECIALS_WEEKLY_BLOCK_KICK_RET_TDS = 40;
  public static final int SPECIALS_WEEKLY_TOTAL_SPECIAL_TEAM_TDS = 41;
  public static final int SPECIALS_WEEKLY_PUNTS = 42;
  public static final int SPECIALS_WEEKLY_PUNTING_YDS = 43;
  public static final int SPECIALS_WEEKLY_PUNTS_INSIDE_THE_20 = 44;
  public static final int SPECIALS_WEEKLY_PUNTS_TOUCHBACKS = 45;
  public static final int SPECIALS_WEEKLY_KICKOFFS = 46;
  public static final int SPECIALS_WEEKLY_KICKOFFS_IN_END_ZONE = 47;
  public static final int SPECIALS_WEEKLY_KICKOFFS_TOUCHBACKS = 48;
  public static final int SPECIALS_WEEKLY_SPECIAL_TEAM_POINTS = 49;

  public static final int DEFENSE_WEEKLY_OPPONENT_ID = 50;
  public static final int DEFENSE_WEEKLY_POINTS_SCORED = 51;
  public static final int DEFENSE_WEEKLY_POINTS_ALLOWED = 52;
  public static final int DEFENSE_WEEKLY_TOTAL_YDS = 53;
  public static final int DEFENSE_WEEKLY_TOTAL_PLAYS = 54;
  public static final int DEFENSE_WEEKLY_PASSING_COMPLETIONS = 55;
  public static final int DEFENSE_WEEKLY_PASSING_ATTEMPS = 56;
  public static final int DEFENSE_WEEKLY_PASSING_YDS = 57;
  public static final int DEFENSE_WEEKLY_PASSING_TDS = 58;
  public static final int DEFENSE_WEEKLY_SACKS = 59;
  public static final int DEFENSE_WEEKLY_SACKED_YDS_LOST = 60;
  public static final int DEFENSE_WEEKLY_PASSED_DEFENDED = 61;
  public static final int DEFENSE_WEEKLY_RUSHING_ATTEMPTS = 62;
  public static final int DEFENSE_WEEKLY_RUSHING_YDS = 63;
  public static final int DEFENSE_WEEKLY_RUSHING_TDS = 64;
  public static final int DEFENSE_WEEKLY_TACKLES_FOR_LOSS = 65;
  public static final int DEFENSE_WEEKLY_TACKLE_FOR_LOSS_YDS = 66;
  public static final int DEFENSE_WEEKLY_INTERCEPTIONS = 67;
  public static final int DEFENSE_WEEKLY_INTERCEPTION_RET_YDS = 68;
  public static final int DEFENSE_WEEKLY_INTERCEPTION_RET_TDS = 69;
  public static final int DEFENSE_WEEKLY_FUMBLES_FORCED = 70;
  public static final int DEFENSE_WEEKLY_FUMBLES_RECOVERED = 71;
  public static final int DEFENSE_WEEKLY_FUMBLES_RET_YDS = 72;
  public static final int DEFENSE_WEEKLY_FUMBLES_RET_TDS = 73;
  public static final int DEFENSE_WEEKLY_SAFTIES = 74;
  public static final int DEFENSE_WEEKLY_TWO_PT_CONVERSIONS = 75;
  public static final int DEFENSE_WEEKLY_PENALTIES = 76;
  public static final int DEFENSE_WEEKLY_PENALTY_YDS = 77;
  public static final int DEFENSE_WEEKLY_PAT_MADE = 78;
  public static final int DEFENSE_WEEKLY_PAT_ATTEMPTED = 79;
  public static final int DEFENSE_WEEKLY_FG_MADE = 80;
  public static final int DEFENSE_WEEKLY_FG_ATTEMPTED = 81;
  public static final int DEFENSE_WEEKLY_FIRST_DOWNS_TOTAL = 82;
  public static final int DEFENSE_WEEKLY_FIRST_DOWNS_RUSHING = 83;
  public static final int DEFENSE_WEEKLY_FIRST_DOWNS_PASSING = 84;
  public static final int DEFENSE_WEEKLY_FIRST_DOWNS_PENALTY = 85;
  public static final int DEFENSE_WEEKLY_THIRD_DOWN_CONVERSIONS = 86;
  public static final int DEFENSE_WEEKLY_THIRD_DOWN_CONVERSIONS_ATTEMPT = 87;
  public static final int DEFENSE_WEEKLY_FOURTH_DOWN_CONVERSIONS = 88;
  public static final int DEFENSE_WEEKLY_FOURTH_DOWN_CONVERSIONS_ATTEMPT = 89;
  public static final int DEFENSE_WEEKLY_DEFENSIVE_TDS_SCORED = 90;
  public static final int DEFENSE_WEEKLY_PTS_SCORED_BY_DEFENSE = 91;
  public static final int DEFENSE_WEEKLY_PTS_SCORED_BY_OPPOSING_DEFENSE = 92;
  public static final int DEFENSE_WEEKLY_TDS_BY_DISTANCE = 93;

  //Types
  private final int MULTIPLY = 1;
  private final int MIN_MAX = 2;
  private final int GRADATED_RATE = 3;

  int id_ = -1;
  int type_ = -1;
  int worth_ = 0;
  int minVal_ = 0;
  int maxVal_ = 0;
  int rate_ = 0;
  String position_ = "";

  public ScoringRule(int id,
		     int type,
		     int worth,
		     int minVal,
		     int maxVal,
		     int rate,
		     String position)
  {
    id_ = id;
    type_ = type;
    worth_ = worth;
    minVal_ = minVal;
    maxVal_ = maxVal;
    rate_ = rate;
    position_ = position;

/*
    System.out.println("Scoring rule constuctor id = " + id_ +
		       " type_ = " + type_ + " worth_ = " + worth_ +
		       " minVal_ = " + minVal_ + " maxVal_ = " + maxVal_ +
		       " rate_ = " + rate_ +
		       " position_ = " + position_);
*/
  }

  public int getID() { return id_; }
  public int getType() { return type_; }
  public int getWorth() { return worth_; }
  public int getMinVal() { return minVal_; }
  public int getMaxVal() { return maxVal_; }
  public int getRate() { return rate_; }
  public String getPosition() { return position_; }

  public int doScore(int value)
  {
    int retval = 0;

    if (type_ == MULTIPLY)
    {
      retval = value * worth_;
    }
    else if (type_ == MIN_MAX)
    {
	if (minVal_ <= value && value <= maxVal_)
	{
	    retval = worth_;
	}
    }
    else if (type_ == GRADATED_RATE)
    {
	if ((value - minVal_) > 0)
	{
	    retval = ((value - minVal_) / rate_ + 1);
	}
    }

    return retval;
  }
}