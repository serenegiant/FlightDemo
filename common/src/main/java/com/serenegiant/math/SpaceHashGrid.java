package com.serenegiant.math;

import com.serenegiant.gl.GameObject;

import java.util.ArrayList;
import java.util.List;

public class SpaceHashGrid {
	private static final int MAX_ID = 8;	// 1つのオブジェクトが同時に存在する可能性のあるcellの最大数 
	private static final int MAX_OBJ = 10;	// 1セルあたりにGameObjectは10個まで
	private final List<GameObject>[] dynamicCells;
	private final List<GameObject>[] staticCells;
	private int cellsPerX;
	private int cellsPerY;
	private int cellsPerZ;
	private int cellsPerXY;
	private float cellSize;					// 1セルの高さ/幅/奥行き
	private final int[] cellIDs = new int[MAX_ID];
	private final List<GameObject> foundObjects;
	private final Vector w = new Vector();

	@SuppressWarnings("unchecked")
	public SpaceHashGrid(final float worldWidth, final float worldHeight, final float worldDepth, final float cellSize) {
		this.cellSize = cellSize;
		this.cellsPerX = (int)Math.ceil(worldWidth / cellSize);
		this.cellsPerY = (int)Math.ceil(worldHeight / cellSize);
		this.cellsPerZ = (int)Math.ceil(worldDepth / cellSize);
		if (cellsPerZ < 1) cellsPerZ = 1;
		cellsPerXY = cellsPerX * cellsPerY;
		final int numCells = cellsPerXY * cellsPerZ;
		dynamicCells = new List[numCells];
		staticCells = new List[numCells];
		for (int i = 0; i < numCells; i++) {
			dynamicCells[i] = new ArrayList<GameObject>(MAX_OBJ);
			staticCells[i] = new ArrayList<GameObject>(MAX_OBJ);
		}
		foundObjects = new ArrayList<GameObject>(MAX_OBJ);
	}

	public SpaceHashGrid(final float worldWidth, final float worldHeight, final float cellSize) {
		this(worldWidth, worldHeight, cellSize, cellSize);
	}
	
	public void insertDynamicObject(final GameObject obj) {
		internal_insertObject(dynamicCells, obj);
	}
	
	public void insertStaticObject(final GameObject obj) {
		internal_insertObject(staticCells, obj);
	}
	
	private void internal_insertObject(final List<GameObject>[]cells, final GameObject obj) {
		checkCellIDs(obj);
		int i = 0;
		int cellID = -1;
		while ((i < MAX_ID) && ((cellID = cellIDs[i++]) != -1)) {
			cells[cellID].add(obj);
		}
	}
	
	public void removeObject(final GameObject obj) {
		checkCellIDs(obj);
		int i = 0;
		int cellID = -1;
		while ((i < MAX_ID) && ((cellID = cellIDs[i++]) != -1)) {
			dynamicCells[cellID].remove(obj);
			staticCells[cellID].remove(obj);
		}	
	}

	public void clearDynamicObject(final GameObject obj) {
		final int len = dynamicCells.length;
		for (int i = 0; i < len; i++) {
			dynamicCells[i].clear();
		}	
	}
	
	public List<GameObject> getPotentialColliders(GameObject obj) {
		foundObjects.clear();
		checkCellIDs(obj);	// オブジェクトの座標から所属するCellのIDを取得する
		int i = 0;
		int cellID = -1;
		while ((i < MAX_ID) && ((cellID = cellIDs[i++]) != -1)) {
			int len = dynamicCells[cellID].size();
			for (int j = 0; j < len; j++) {
				GameObject collider = dynamicCells[cellID].get(j);
				if (!foundObjects.contains(collider)) {
					foundObjects.add(collider);
				}
			}
			len = staticCells[cellID].size();
			for (int j = 0; j < len; j++) {
				GameObject collider = staticCells[cellID].get(j);
				if (!foundObjects.contains(collider)) {
					foundObjects.add(collider);
				}
			}
		}
		return foundObjects;
	}

	private void checkCellIDs(final GameObject obj) {
		if (cellsPerZ == 1)
			checkCellIDsXY(obj);		// 2Dの時
		else
			checkCellIDsXYZ(obj);		// 3Dの時
	}
	
	private void checkCellIDsXYZ(final GameObject obj) {
		w.set(obj.bounds.position);
		final float r = obj.bounds.radius;
		final int x1 = (int)Math.floor((w.x - r) / cellSize);
		final int y1 = (int)Math.floor((w.y - r) / cellSize);
		final int z1 = (int)Math.floor((w.z - r) / cellSize);
		final int x2 = (int)Math.floor((w.x + r) / cellSize);
		final int y2 = (int)Math.floor((w.y + r) / cellSize);
		final int z2 = (int)Math.floor((w.z + r) / cellSize);
		
		for (int i = 0; i < MAX_ID; i++)
			cellIDs[i] = -1;

		if ((x1 == x2) && (y1 == y2)) {	// 真ん中
			if (z1 == z2) {
				if ((x1 >= 0) && (x1 < cellsPerX)
					&& (y1 >= 0) && (y1 < cellsPerY)
					&& (z1 >= 0) && (z2 < cellsPerZ)) 
					cellIDs[0] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
			} else {
				int i = 0;
				if ((z1 >= 0) && (z1 < cellsPerZ))
					cellIDs[i++] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
				if ((z2 >= 0) && (z2 < cellsPerZ))
					cellIDs[i++] = x1 + y1 * cellsPerX + z2 * cellsPerXY;
			}
		} else if (x1 == x2) {
			int i = 0;
			if ((x1 >= 0) && (x1 < cellsPerX)) {
				if ((y1 >= 0) && (y1 < cellsPerY)) {
					if (z1 == z2) {
						cellIDs[i++] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
					} else {
						if ((z1 >= 0) && (z1 < cellsPerZ))
							cellIDs[i++] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
						if ((z2 >= 0) && (z2 < cellsPerZ))
							cellIDs[i++] = x1 + y1 * cellsPerX + z2 * cellsPerXY;
					}
				}
				if ((y2 >= 0) && (y2 < cellsPerY)) {
					if (z1 == z2) {
						cellIDs[i++] = x1 + y2 * cellsPerX + z1 * cellsPerXY;
					} else {
						if ((z1 >= 0) && (z1 < cellsPerZ))
							cellIDs[i++] = x1 + y2 * cellsPerX + z1 * cellsPerXY;
						if ((z2 >= 0) && (z2 < cellsPerZ))
							cellIDs[i++] = x1 + y2 * cellsPerX + z2 * cellsPerXY;
					}
				}
			}
		} else if (y1 == y2) {
			int i = 0;
			if ((y1 >= 0) && (y1 < cellsPerY)) {
				if ((x1 >= 0) && (x1 < cellsPerX)) {
					if (z1 == z2) {
						cellIDs[i++] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
					} else {
						if ((z1 >= 0) && (z1 < cellsPerZ))
							cellIDs[i++] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
						if ((z2 >= 0) && (z2 < cellsPerZ))
							cellIDs[i++] = x1 + y1 * cellsPerX + z2 * cellsPerXY;
					}
				}
				if ((x2 >= 0) && (x2 < cellsPerX)) {
					if (z1 == z2) {
						cellIDs[i++] = x2 + y1 * cellsPerX + z1 * cellsPerXY;
					} else {
						if ((z1 >= 0) && (z1 < cellsPerZ))
							cellIDs[i++] = x2 + y1 * cellsPerX + z1 * cellsPerXY;
						if ((z2 >= 0) && (z2 < cellsPerZ))
							cellIDs[i++] = x2 + y1 * cellsPerX + z2 * cellsPerXY;
					}
				}
			}
		} else {
			int i = 0;	
			if ((x1 >= 0) && (x1 < cellsPerX) && (y1 >= 0) && (y1 < cellsPerY)) {
				if (z1 == z2) {
					cellIDs[i++] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
				} else {
					if ((z1 >= 0) && (z1 < cellsPerZ))
						cellIDs[i++] = x1 + y1 * cellsPerX + z1 * cellsPerXY;
					if ((z2 >= 0) && (z2 < cellsPerZ))
						cellIDs[i++] = x1 + y1 * cellsPerX + z2 * cellsPerXY;
				}
			}
			if ((x2 >= 0) && (x2 < cellsPerX) && (y1 >= 0) && (y1 < cellsPerY)) {
				if (z1 == z2) {
					cellIDs[i++] = x2 + y1 * cellsPerX + z1 * cellsPerXY;
				} else {
					if ((z1 >= 0) && (z1 < cellsPerZ))
						cellIDs[i++] = x2 + y1 * cellsPerX + z1 * cellsPerXY;
					if ((z2 >= 0) && (z2 < cellsPerZ))
						cellIDs[i++] = x2 + y1 * cellsPerX + z2 * cellsPerXY;
				}
			}
			if ((x2 >= 0) && (x2 < cellsPerX) && (y2 >= 0) && (y2 < cellsPerY)) {
				if (z1 == z2) {
					cellIDs[i++] = x2 + y2 * cellsPerX + z1 * cellsPerXY;
				} else {
					if ((z1 >= 0) && (z1 < cellsPerZ))
						cellIDs[i++] = x2 + y2 * cellsPerX + z1 * cellsPerXY;
					if ((z2 >= 0) && (z2 < cellsPerZ))
						cellIDs[i++] = x2 + y2 * cellsPerX + z2 * cellsPerXY;
				}
			}
			if ((x1 >= 0) && (x1 < cellsPerX) && (y2 >= 0) && (y2 < cellsPerY)) {
				if (z1 == z2) {
					cellIDs[i++] = x1 + y2 * cellsPerX + z1 * cellsPerXY;
				} else {
					if ((z1 >= 0) && (z1 < cellsPerZ))
						cellIDs[i++] = x1 + y2 * cellsPerX + z1 * cellsPerXY;
					if ((z2 >= 0) && (z2 < cellsPerZ))
						cellIDs[i++] = x1 + y2 * cellsPerX + z2 * cellsPerXY;
				}
			}
		}
	}

	private void checkCellIDsXY(final GameObject obj) {
		w.set(obj.bounds.position);
		final float r = obj.bounds.radius;
		final int x1 = (int)Math.floor((w.x - r) / cellSize);
		final int y1 = (int)Math.floor((w.y - r) / cellSize);
		final int x2 = (int)Math.floor((w.x + r) / cellSize);
		final int y2 = (int)Math.floor((w.y + r) / cellSize);
		
		for (int i = 0; i < MAX_ID; i++)
			cellIDs[i] = -1;

		if ((x1 == x2) && (y1 == y2)) {	// 真ん中
			if ((x1 >= 0) && (x1 < cellsPerX)
				&& (y1 >= 0) && (y1 < cellsPerY)) {
				cellIDs[0] = x1 + y1 * cellsPerX;
			}
		} else if (x1 == x2) {
			int i = 0;
			if ((x1 >= 0) && (x1 < cellsPerX)) {
				if ((y1 >= 0) && (y1 < cellsPerY)) {
					cellIDs[i++] = x1 + y1 * cellsPerX;
				}
				if ((y2 >= 0) && (y2 < cellsPerY)) {
					cellIDs[i++] = x1 + y2 * cellsPerX;
				}
			}
		} else if (y1 == y2) {
			int i = 0;
			if ((y1 >= 0) && (y1 < cellsPerY)) {
				if ((x1 >= 0) && (x1 < cellsPerX)) {
					cellIDs[i++] = x1 + y1 * cellsPerX;
				}
				if ((x2 >= 0) && (x2 < cellsPerX)) {
					cellIDs[i++] = x2 + y1 * cellsPerX;
				}
			}
		} else {
			int i = 0;	
			if ((x1 >= 0) && (x1 < cellsPerX) && (y1 >= 0) && (y1 < cellsPerY)) {
				cellIDs[i++] = x1 + y1 * cellsPerX;
			}
			if ((x2 >= 0) && (x2 < cellsPerX) && (y1 >= 0) && (y1 < cellsPerY)) {
				cellIDs[i++] = x2 + y1 * cellsPerX;
			}
			if ((x2 >= 0) && (x2 < cellsPerX) && (y2 >= 0) && (y2 < cellsPerY)) {
				cellIDs[i++] = x2 + y2 * cellsPerX;
			}
			if ((x1 >= 0) && (x1 < cellsPerX) && (y2 >= 0) && (y2 < cellsPerY)) {
				cellIDs[i++] = x1 + y2 * cellsPerX;
			}
		}
	}
}
