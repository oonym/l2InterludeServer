/* This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package net.sf.l2j.gameserver.datatables;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import javolution.util.FastList;
import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.instancemanager.ArenaManager;
import net.sf.l2j.gameserver.instancemanager.FishingZoneManager;
import net.sf.l2j.gameserver.instancemanager.OlympiadStadiaManager;
import net.sf.l2j.gameserver.instancemanager.TownManager;
import net.sf.l2j.gameserver.model.L2World;
import net.sf.l2j.gameserver.model.L2WorldRegion;
import net.sf.l2j.gameserver.model.zone.L2ZoneType;
import net.sf.l2j.gameserver.model.zone.form.ZoneCuboid;
import net.sf.l2j.gameserver.model.zone.form.ZoneNPoly;
import net.sf.l2j.gameserver.model.zone.type.L2ArenaZone;
import net.sf.l2j.gameserver.model.zone.type.L2BigheadZone;
import net.sf.l2j.gameserver.model.zone.type.L2CastleZone;
import net.sf.l2j.gameserver.model.zone.type.L2ClanHallZone;
import net.sf.l2j.gameserver.model.zone.type.L2DamageZone;
import net.sf.l2j.gameserver.model.zone.type.L2DerbyTrackZone;
import net.sf.l2j.gameserver.model.zone.type.L2FishingZone;
import net.sf.l2j.gameserver.model.zone.type.L2JailZone;
import net.sf.l2j.gameserver.model.zone.type.L2MotherTreeZone;
import net.sf.l2j.gameserver.model.zone.type.L2NoLandingZone;
import net.sf.l2j.gameserver.model.zone.type.L2OlympiadStadiumZone;
import net.sf.l2j.gameserver.model.zone.type.L2PeaceZone;
import net.sf.l2j.gameserver.model.zone.type.L2TownZone;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This class manages the augmentation data and can also create new augmentations.
 *
 * @author  durgus
 */
public class ZoneData
{
	private static final Logger _log = Logger.getLogger(ZoneData.class.getName());

	// =========================================================
	private static ZoneData _instance;

	public static final ZoneData getInstance()
	{
		if (_instance == null)
		{
			_instance = new ZoneData();
		}
		return _instance;
	}

	// =========================================================
	// Data Field

	// =========================================================
	// Constructor
	public ZoneData()
	{
		_log.info("Loading zones...");

		load();
	}


	// =========================================================
	// Method - Private

	private final void load()
	{
		java.sql.Connection con = null;
		int zoneCount = 0;

		// Get the world regions
		L2WorldRegion[][] worldRegions = L2World.getInstance().getAllWorldRegions();

		// Load the zone xml
		try
		{
			// Get a sql connection here
			con = L2DatabaseFactory.getInstance().getConnection();

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);

			File file = new File(Config.DATAPACK_ROOT+"/data/zones/zone.xml");
			if (!file.exists())
			{
				if (Config.DEBUG)
					_log.info("The zone.xml file is missing.");
				return;
			}

			Document doc = factory.newDocumentBuilder().parse(file);

			for (Node n=doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				if ("list".equalsIgnoreCase(n.getNodeName()))
				{
					for (Node d=n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if ("zone".equalsIgnoreCase(d.getNodeName()))
						{
							NamedNodeMap attrs = d.getAttributes();
							int zoneId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
							int minZ = Integer.parseInt(attrs.getNamedItem("minZ").getNodeValue());
							int maxZ = Integer.parseInt(attrs.getNamedItem("maxZ").getNodeValue());
							String zoneType = attrs.getNamedItem("type").getNodeValue();
							String zoneShape = attrs.getNamedItem("shape").getNodeValue();

							// Create the zone
							L2ZoneType temp = null;

							if (zoneType.equals("FishingZone"))
								 temp = new L2FishingZone();
							else if (zoneType.equals("ClanHallZone"))
								 temp = new L2ClanHallZone();
							else if (zoneType.equals("PeaceZone"))
								temp = new L2PeaceZone();
							else if (zoneType.equals("Town"))
								temp = new L2TownZone();
							else if (zoneType.equals("OlympiadStadium"))
								temp = new L2OlympiadStadiumZone();
							else if (zoneType.equals("CastleZone"))
								temp = new L2CastleZone();
							else if (zoneType.equals("DamageZone"))
								temp = new L2DamageZone();
							else if (zoneType.equals("Arena"))
								temp = new L2ArenaZone();
							else if (zoneType.equals("MotherTree"))
								temp = new L2MotherTreeZone();
							else if (zoneType.equals("BigheadZone"))
								temp = new L2BigheadZone();
							else if (zoneType.equals("NoLandingZone"))
								temp = new L2NoLandingZone();
							else if (zoneType.equals("JailZone"))
								temp = new L2JailZone();
							else if (zoneType.equals("DerbyTrackZone"))
								temp = new L2DerbyTrackZone();


							// Check for unknown type
							if (temp == null)
							{
								_log.warning("ZoneData: No such zone type: "+zoneType);
								continue;
							}

							// Get the zone shape from sql
							try
							{
								PreparedStatement statement = null;

								// Set the correct query
								statement = con.prepareStatement("SELECT x,y FROM zone_vertices WHERE id=? ORDER BY 'order' ASC ");

								statement.setInt(1, zoneId);
								ResultSet rset = statement.executeQuery();

								// Create this zone.  Parsing for cuboids is a bit different than for other polygons
								// cuboids need exactly 2 points to be defined.  Other polygons need at least 3 (one per vertex)
								if (zoneShape.equals("Cuboid"))
								{
									int[] x = {0,0};
									int[] y = {0,0};
									boolean successfulLoad = true;
									
									for (int i=0;i<2; i++)
									{
										if ( rset.next() )
										{
											x[i] = rset.getInt("x");
											y[i] = rset.getInt("y");
										}
										else
										{
											_log.warning("ZoneData: Missing cuboid vertex in sql data for zone: "+zoneId);
											rset.close();
											statement.close();
											successfulLoad = false;
											break;
										}
									}

									if (successfulLoad)
										temp.setZone(new ZoneCuboid(x[0],x[1], y[0],y[1],minZ,maxZ));
									else
										continue;
								}
								else if (zoneShape.equals("NPoly"))
								{
									FastList<Integer> fl_x = new FastList<Integer>(), fl_y = new FastList<Integer>();

									// Load the rest
									while (rset.next())
									{
										fl_x.add(rset.getInt("x"));
										fl_y.add(rset.getInt("y"));
									}

									// An nPoly needs to have at least 3 vertices
									if ((fl_x.size() == fl_y.size()) && (fl_x.size() > 2))
									{
										// Create arrays
										int[] aX = new int[fl_x.size()];
										int[] aY = new int[fl_y.size()];
	
										// This runs only at server startup so dont complain :>
										for (int i=0; i < fl_x.size(); i++) { aX[i] = fl_x.get(i); aY[i] = fl_y.get(i); }
	
										// Create the zone
										temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
									}
									else
									{
										_log.warning("ZoneData: Bad sql data for zone: "+zoneId);
										rset.close();
										statement.close();
										continue;
									}
								}
								else
								{
									_log.warning("ZoneData: Unknown shape: "+zoneShape);
									rset.close();
									statement.close();
									continue;
								}

								rset.close();
								statement.close();
							}
							catch (Exception e)
							{
								_log.warning("ZoneData: Failed to load zone coordinates: " + e);
							}


							// Check for aditional parameters
							for (Node cd=d.getFirstChild(); cd != null; cd = cd.getNextSibling())
							{
								if ("stat".equalsIgnoreCase(cd.getNodeName()))
								{
									attrs = cd.getAttributes();
									String name = attrs.getNamedItem("name").getNodeValue();
                            		String val = attrs.getNamedItem("val").getNodeValue();

                            		temp.setParameter(name, val);
								}
							}

							// Skip checks for fishing zones & add to fishing zone manager
							if (temp instanceof L2FishingZone)
							{
								FishingZoneManager.getInstance().addFishingZone((L2FishingZone)temp);
								continue;
							}

							// Register the zone into any world region it intersects with...
							// currently 11136 test for each zone :>
							int ax,ay,bx,by;
							for (int x=0; x < worldRegions.length; x++)
							{
								for (int y=0; y < worldRegions[x].length; y++)
								{
									ax = (x-L2World.OFFSET_X) << L2World.SHIFT_BY;
									bx = ((x+1)-L2World.OFFSET_X) << L2World.SHIFT_BY;
									ay = (y-L2World.OFFSET_Y) << L2World.SHIFT_BY;
									by = ((y+1)-L2World.OFFSET_Y) << L2World.SHIFT_BY;

									if (temp.getZone().intersectsRectangle(ax, bx, ay, by))
									{
										if (Config.DEBUG)
										{
											_log.info("Zone ("+zoneId+") added to: "+x+" "+y);
										}
										worldRegions[x][y].addZone(temp);
									}
								}
							}

							// Special managers for arenas, towns...
							if (temp instanceof L2ArenaZone)
								ArenaManager.getInstance().addArena((L2ArenaZone)temp);
							else if (temp instanceof L2TownZone)
								TownManager.getInstance().addTown((L2TownZone)temp);
							else if (temp instanceof L2OlympiadStadiumZone)
								OlympiadStadiaManager.getInstance().addStadium((L2OlympiadStadiumZone)temp);

							// Increase the counter
							zoneCount++;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.SEVERE, "Error while loading zones.", e);
			return ;
		}
		finally
		{
			try
			{
				con.close();
			}
			catch (Exception e)
			{
			}
		}

		_log.info("Done: loaded "+zoneCount+" zones.");
	}
}
