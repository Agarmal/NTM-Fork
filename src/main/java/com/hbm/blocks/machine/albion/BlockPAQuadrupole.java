package com.hbm.blocks.machine.albion;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.albion.TileEntityPAQuadrupole;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPAQuadrupole extends BlockDummyable {

	public BlockPAQuadrupole() {
		super(Material.iron);
		this.setCreativeTab(MainRegistry.machineTab);
		this.setBlockTextureName(RefStrings.MODID + ":block_steel");
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityPAQuadrupole();
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {1, 1, 1, 1, 1, 1}; }
	@Override public int getOffset() { return 0; }
	@Override public int getHeightOffset() { return 1; }
}
