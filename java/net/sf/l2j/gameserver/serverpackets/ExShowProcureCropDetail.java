package net.sf.l2j.gameserver.serverpackets;

import javolution.util.FastMap;
import net.sf.l2j.gameserver.instancemanager.CastleManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager;
import net.sf.l2j.gameserver.instancemanager.CastleManorManager.CropProcure;
import net.sf.l2j.gameserver.model.entity.Castle;


/**
 * format(packet 0xFE)
 * ch dd [dddc]
 * c  - id
 * h  - sub id
 *
 * d  - crop id
 * d  - size
 *
 * [
 * d  - manor name
 * d  - buy residual
 * d  - buy price
 * c  - reward type
 * ]
 *
 * @author l3x
 */
public class ExShowProcureCropDetail extends L2GameServerPacket {
	private static final String _S__FE_22_EXSHOWPROCURECROPDETAIL = "[S] FE:22 ExShowProcureCropDetail";

	private int _cropId;
	private FastMap<Integer, CropProcure> _castleCrops;

	public ExShowProcureCropDetail(int cropId) {
		_cropId = cropId;
		_castleCrops = new FastMap<Integer, CropProcure>();

		for (Castle c : CastleManager.getInstance().getCastles()) {
			CropProcure cropItem = c.getCrop(_cropId, CastleManorManager.PERIOD_CURRENT);
			if (cropItem != null && cropItem.getAmount() > 0) {
				_castleCrops.put(c.getCastleId(), cropItem);
			}
		}
	}

	@Override
	public void runImpl() {
	}

	@Override
	public void writeImpl() {
		writeC(0xFE);
		writeH(0x22);

		writeD(_cropId); // crop id
		writeD(_castleCrops.size());       // size

		for (int manorId : _castleCrops.keySet()) {
			CropProcure crop = _castleCrops.get(manorId);
			writeD(manorId);          // manor name
			writeD(crop.getAmount()); // buy residual
			writeD(crop.getPrice());  // buy price
			writeC(crop.getReward()); // reward type
		}
	}

	@Override
	public String getType() {
		return _S__FE_22_EXSHOWPROCURECROPDETAIL;
	}

}
