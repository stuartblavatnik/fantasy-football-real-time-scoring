package com.twoforboth.realtimescoring.events;

import com.twoforboth.realtimescoring.NFLGameChecker;
import java.util.EventObject;

/**
 * <p>Title: GameStatsEvent.java</p>
 * <p>Description: Event triggered when stats are found for an NFL game</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class GameStatsEvent extends EventObject
{
  protected NFLGameChecker gameChecker_ = null;     //The object that triggered this event

  /**
   * Constructor
   * @param source Object that triggered this event
   * @param gameChecker Object that triggered this event
   */

  public GameStatsEvent(Object source, NFLGameChecker gameChecker)
  {
    super(source);
    gameChecker_ = gameChecker;
  }

  /**
   * Retrieves the object that triggered this event
   * @returns NFLGameChecker object
   */

  public NFLGameChecker getGameNFLChecker()
  {
    return gameChecker_;
  }
}