/**
 * 
 */
package net.sf.l2j.gameserver.model;

/**
 * @author zabbix
 *
 */
public class CropProcure
{
    private int _cropId;
    private int _canBuy;
    private int _rewardType;
    
    public CropProcure(int id, int amount, int type)
    {
        _cropId = id;
        _canBuy = amount;
        _rewardType = type;
    }
    
    public int getReward(){return _rewardType;}
    public int getId(){return _cropId;}
    public int getAmount(){return _canBuy;}
}
