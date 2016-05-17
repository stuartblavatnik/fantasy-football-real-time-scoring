package com.twoforboth.realtimescoring.events;

import java.util.EventObject;

import com.twoforboth.realtimescoring.NFLGameChecker;

/**
 * <p>Title: Scrambled Eggs Football Real Time Scoring</p>
 * <p>Description: Real Time Football Updates</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Two For Both Inc</p>
 * @author Stuart Blavatnik
 * @version 1.0
 */

public class GameEndedEvent extends EventObject
{
  protected NFLGameChecker gameChecker_ = null;     //The object that triggered this event

  /**
   * Constructor
   * @param source Object that triggered this event
   * @param gameChecker Object that triggered this event
   */

  public GameEndedEvent(Object source, NFLGameChecker gameChecker)
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