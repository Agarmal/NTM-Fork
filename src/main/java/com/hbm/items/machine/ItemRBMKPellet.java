package com.hbm.items.machine;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;

public class ItemRBMKPellet extends Item {
	
	public String fullName = "";

	public ItemRBMKPellet(String fullName) {
		this.fullName = fullName;
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tabs, List list) {
		for(int i = 0; i < 10; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
	}
	
	@SideOnly(Side.CLIENT)
	private IIcon[] enrichmentOverlays = new IIcon[5];
	private IIcon xenonOverlay;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister p_94581_1_) {
		super.registerIcons(p_94581_1_);
		
		for(int i = 0; i < enrichmentOverlays.length; i++) {
			enrichmentOverlays[i] = p_94581_1_.registerIcon("hbm:rbmk_pellet_overlay_e" + i);
		}
		
		xenonOverlay = p_94581_1_.registerIcon("hbm:rbmk_pellet_overlay_xenon");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public int getRenderPasses(int meta) {
		return hasXenon(meta) ? 3 : 2;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(stack, player, list, bool);
		
		list.add(EnumChatFormatting.ITALIC + this.fullName);
		
		int meta = rectify(stack.getItemDamage());
		
		switch(meta % 5) {
		case 0: list.add(EnumChatFormatting.GOLD + "Brand New"); break;
		case 1: list.add(EnumChatFormatting.YELLOW + "Barely Depleted"); break;
		case 2: list.add(EnumChatFormatting.GREEN + "Moderately Depleted"); break;
		case 3: list.add(EnumChatFormatting.DARK_GREEN + "Highly Depleted"); break;
		case 4: list.add(EnumChatFormatting.DARK_GRAY + "Fully Depleted"); break;
		}
		
		if(hasXenon(meta))
			list.add(EnumChatFormatting.DARK_PURPLE + "High Xenon Poison");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		
		if(pass == 0)
			return this.itemIcon;
		
		if(pass == 2)
			return this.xenonOverlay;
		
		return this.enrichmentOverlays[rectify(meta) % 5];
	}
	
	private boolean hasXenon(int meta) {
		return rectify(meta) >= 5;
	}
	
	private int rectify(int meta) {
		return Math.abs(meta) % 10;
	}
}