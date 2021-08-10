package com.hbm.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.config.RadiationConfig;
import com.hbm.explosion.ExplosionNukeSmall;
import com.hbm.extprop.HbmLivingProps;
import com.hbm.extprop.HbmLivingProps.ContaminationEffect;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.AuxParticlePacketNT;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.ExtPropPacket;
import com.hbm.saveddata.AuxSavedData;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class EntityEffectHandler {

	public static void onUpdate(EntityLivingBase entity) {
		
		if(!entity.worldObj.isRemote) {
			
			if(entity.ticksExisted % 20 == 0) {
				HbmLivingProps.setRadBuf(entity, HbmLivingProps.getRadEnv(entity));
				HbmLivingProps.setRadEnv(entity, 0);
			}
			
			
			if(entity instanceof EntityPlayerMP) {
				HbmLivingProps props = HbmLivingProps.getData(entity);
				NBTTagCompound data = new NBTTagCompound();
				props.saveNBTData(data);
				PacketDispatcher.wrapper.sendTo(new ExtPropPacket(data), (EntityPlayerMP) entity);
			}
			
			int timer = HbmLivingProps.getTimer(entity);
			if(timer > 0) {
				HbmLivingProps.setTimer(entity, timer - 1);
				
				if(timer == 1) {
					ExplosionNukeSmall.explode(entity.worldObj, entity.posX, entity.posY, entity.posZ, ExplosionNukeSmall.medium);
				}
			}
		}

		handleContamination(entity);
		handleContagion(entity);
		handleRadiation(entity);
		handleDigamma(entity);
	}
	
	private static void handleContamination(EntityLivingBase entity) {
		
		if(entity.worldObj.isRemote)
			return;
		
		List<ContaminationEffect> contamination = HbmLivingProps.getCont(entity);
		List<ContaminationEffect> rem = new ArrayList();
		
		for(ContaminationEffect con : contamination) {
			ContaminationUtil.contaminate(entity, HazardType.RADIATION, con.ignoreArmor ? ContaminationType.RAD_BYPASS : ContaminationType.CREATIVE, con.getRad());
			
			con.time--;
			
			if(con.time <= 0)
				rem.add(con);
		}
		
		contamination.removeAll(rem);
	}
	
	private static void handleRadiation(EntityLivingBase entity) {
		
		if(ContaminationUtil.isRadImmune(entity))
			return;
		
		World world = entity.worldObj;
		
		if(!world.isRemote) {
			
			int ix = (int)MathHelper.floor_double(entity.posX);
			int iy = (int)MathHelper.floor_double(entity.posY);
			int iz = (int)MathHelper.floor_double(entity.posZ);
	
			float rad = ChunkRadiationManager.proxy.getRadiation(world, ix, iy, iz);
	
			if(world.provider.isHellWorld && RadiationConfig.hellRad > 0 && rad < RadiationConfig.hellRad)
				rad = RadiationConfig.hellRad;
	
			if(rad > 0) {
				ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, rad / 20F);
			}
	
			if(entity.worldObj.isRaining() && RadiationConfig.cont > 0 && AuxSavedData.getThunder(entity.worldObj) > 0 && entity.worldObj.canBlockSeeTheSky(ix, iy, iz)) {
				
				ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, RadiationConfig.cont * 0.0005F);
			}
			
			if(entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode)
				return;
			
			Random rand = new Random(entity.getEntityId());
			
			if(HbmLivingProps.getRadiation(entity) > 600 && (world.getTotalWorldTime() + rand.nextInt(600)) % 600 == 0) {
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "bloodvomit");
				nbt.setInteger("entity", entity.getEntityId());
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
				
				world.playSoundEffect(ix, iy, iz, "hbm:entity.vomit", 1.0F, 1.0F);
				entity.addPotionEffect(new PotionEffect(Potion.hunger.id, 60, 19));
			} else if(HbmLivingProps.getRadiation(entity) > 200 && (world.getTotalWorldTime() + rand.nextInt(1200)) % 1200 == 0) {
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "vomit");
				nbt.setInteger("entity", entity.getEntityId());
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
				
				world.playSoundEffect(ix, iy, iz, "hbm:entity.vomit", 1.0F, 1.0F);
				entity.addPotionEffect(new PotionEffect(Potion.hunger.id, 60, 19));
			
			}
			
			if(HbmLivingProps.getRadiation(entity) > 900 && (world.getTotalWorldTime() + rand.nextInt(10)) % 10 == 0) {
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "sweat");
				nbt.setInteger("count", 1);
				nbt.setInteger("block", Block.getIdFromBlock(Blocks.redstone_block));
				nbt.setInteger("entity", entity.getEntityId());
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(nbt, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			
			}
		} else {
			float radiation = HbmLivingProps.getRadiation(entity);
			
			if(entity instanceof EntityPlayer && radiation > 600) {
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setString("type", "radiation");
				nbt.setInteger("count", radiation > 900 ? 4 : radiation > 800 ? 2 : 1);
				MainRegistry.proxy.effectNT(nbt);
			}
		}
	}
	
	private static void handleDigamma(EntityLivingBase entity) {
		
		if(!entity.worldObj.isRemote) {
			
			float digamma = HbmLivingProps.getDigamma(entity);
			
			if(digamma < 0.01F)
				return;
			
			int chance = Math.max(10 - (int)(digamma), 1);
			
			if(chance == 1 || entity.getRNG().nextInt(chance) == 0) {
				
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "sweat");
				data.setInteger("count", 1);
				data.setInteger("block", Block.getIdFromBlock(Blocks.soul_sand));
				data.setInteger("entity", entity.getEntityId());
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, 0, 0, 0),  new TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 25));
			}
		}
	}
	
	private static void handleContagion(EntityLivingBase entity) {
		
		World world = entity.worldObj;
		
		if(!entity.worldObj.isRemote) {
			
			Random rand = entity.getRNG();
			int minute = 60 * 20;
			int hour = 60 * minute;
			
			int contagion = HbmLivingProps.getContagion(entity);
			
			if(entity instanceof EntityPlayer) {
				
				EntityPlayer player = (EntityPlayer) entity;
				int randSlot = rand.nextInt(player.inventory.mainInventory.length);
				ItemStack stack = player.inventory.getStackInSlot(randSlot);
				
				if(rand.nextInt(100) == 0) {
					stack = player.inventory.armorItemInSlot(rand.nextInt(4));
				}
				
				//only affect unstackables (e.g. tools and armor) so that the NBT tag's stack restrictions isn't noticeable
				if(stack != null && stack.getMaxStackSize() == 1) {
					
					if(contagion > 0) {
						
						if(!stack.hasTagCompound())
							stack.stackTagCompound = new NBTTagCompound();
						
						stack.stackTagCompound.setBoolean("ntmContagion", true);
						
					} else {
						
						if(stack.hasTagCompound() && stack.stackTagCompound.getBoolean("ntmContagion")) {
							HbmLivingProps.setContagion(player, 3 * hour);
						}
					}
				}
			}
			
			if(contagion > 0) {
				HbmLivingProps.setContagion(entity, contagion - 1);
				
				//aerial transmission only happens once a second 5 minutes into the contagion
				if(contagion < (2 * hour + 55 * minute) && contagion % 20 == 0) {
					
					double range = entity.isWet() ? 16D : 2D; //avoid rain, just avoid it
					
					List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(entity, entity.boundingBox.expand(range, range, range));
					
					for(Entity ent : list) {
						
						if(ent instanceof EntityLivingBase) {
							EntityLivingBase living = (EntityLivingBase) ent;
							if(HbmLivingProps.getContagion(living) <= 0) {
								HbmLivingProps.setContagion(living, 3 * hour);
							}
						}
						
						if(ent instanceof EntityItem) {
							ItemStack stack = ((EntityItem)ent).getEntityItem();
							
							if(!stack.hasTagCompound())
								stack.stackTagCompound = new NBTTagCompound();
							
							stack.stackTagCompound.setBoolean("ntmContagion", true);
						}
					}
				}
				
				//one hour in, add rare and subtle screen fuckery
				if(contagion < 2 * hour && rand.nextInt(1000) == 0) {
					entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 20, 0));
				}
				
				//two hours in, give 'em the full blast
				if(contagion < 1 * hour && rand.nextInt(100) == 0) {
					entity.addPotionEffect(new PotionEffect(Potion.confusion.id, 60, 0));
					entity.addPotionEffect(new PotionEffect(Potion.weakness.id, 300, 4));
				}
				
				//T-30 minutes, take damage every 20 seconds
				if(contagion < 30 * minute && rand.nextInt(400) == 0) {
					entity.attackEntityFrom(DamageSource.magic, 1F);
				}
				
				//T-5 minutes, take damage every 5 seconds
				if(contagion < 5 * minute && rand.nextInt(100) == 0) {
					entity.attackEntityFrom(DamageSource.magic, 2F);
				}
				
				//end of contagion, drop dead
				if(contagion == 0) {
					entity.attackEntityFrom(DamageSource.magic, 1000F);
				}
			}
		}
	}
}
