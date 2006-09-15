package net.sf.l2j.gameserver.model;

/**
/*
 * Author: -Nemesiss-
 *
 */
public class FishDropData
{
	private final int _fishId;
	private final int _rewarditemId;
	private final int _drop;
	private final int _minchance;
	private final int _maxchance;

	public FishDropData(int fishid, int itemid, int drop, int minchance, int maxchance)
	{
		_fishId = fishid;
		_rewarditemId = itemid;
		_drop = drop;
		_minchance = minchance;
		_maxchance = maxchance;

	}
	public int getFishId()
	{
		return _fishId;
	}
	public int getRewardItemId()
	{
		return _rewarditemId;
	}

	/**
	 * Returns the quantity of items dropped
	 * @return int
	 */
	public int getCount()
	{
		return _drop;
	}

	/**
	 * Returns the chance of having a drop
	 * @return int
	 */
	public int getMinChance()
	{
		return _minchance;
	}
	/**
	 * Returns the chance of having a drop
	 * @return int
	 */
	public int getMaxChance()
	{
		return _maxchance;
	}

}

