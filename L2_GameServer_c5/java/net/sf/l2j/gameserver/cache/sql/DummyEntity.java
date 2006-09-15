/**
 * 
 */
package net.sf.l2j.gameserver.cache.sql;

/**
 * @author la2
 *
 */
public class DummyEntity
{
	private int _objectId;
	private int _var1;
	private String _var2;

	public DummyEntity()
	{
		System.err.println("hi zabbox :o)");
	}
	
	public DummyEntity(int objId, int var1, String var2)
	{
		
	}

	/**
	 * @param objectId The objectId to set.
	 */
	public void setObjectId(int objectId)
	{
		_objectId = objectId;
	}

	/**
	 * @return Returns the objectId.
	 */
	public int getObjectId()
	{
		return _objectId;
	}

	/**
	 * @param var1 The var1 to set.
	 */
	public void setVar1(int var1)
	{
		_var1 = var1;
	}

	/**
	 * @return Returns the var1.
	 */
	public int getVar1()
	{
		return _var1;
	}

	/**
	 * @param var2 The var2 to set.
	 */
	public void setVar2(String var2)
	{
		_var2 = var2;
	}

	/**
	 * @return Returns the var2.
	 */
	public String getVar2()
	{
		return _var2;
	}
}
