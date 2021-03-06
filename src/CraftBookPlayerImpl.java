// $Id$
/*
 * CraftBook
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

import com.sk89q.craftbook.*;

/**
 *
 * @author sk89q
 */
public class CraftBookPlayerImpl extends CraftBookPlayer {
    /**
     * Stores the player.
     */
    private Player player;

    /**
     * Construct the object.
     * 
     * @param player
     */
    public CraftBookPlayerImpl(Player player) {
        this.player = player;
    }

    /**
     * Move the player.
     *
     * @param pos
     */
    @Override
    public void setPosition(Vector pos) {
        setPosition(pos, (float)getPitch(), (float)getYaw());
    }

    /**
     * Get the point of the block that is being stood in.
     *
     * @return point
     */
    @Override
    public Vector getBlockIn() {
        return Vector.toBlockPoint(player.getX(), player.getY(), player.getZ());
    }

    /**
     * Get the point of the block that is being stood upon.
     *
     * @return point
     */
    @Override
    public Vector getBlockOn() {
        return Vector.toBlockPoint(player.getX(), player.getY() - 1, player.getZ());
    }

    /**
     * Get the name of the player.
     *
     * @return String
     */
    @Override
    public String getName() {
        return player.getName();
    }
    
    /**
     * Get the world type the player is in.
     *
     * @return int
     */
    @Override
    public CraftBookWorld getCBWorld() {
        return CraftBook.getCBWorld(player.getWorld());
    }

    /**
     * Get the player's view pitch.
     *
     * @return pitch
     */
    @Override
    public double getPitch() {
        return player.getPitch();
    }

    /**
     * Get the player's position.
     *
     * @return point
     */
    @Override
    public Vector getPosition() {
        return new Vector(player.getX(), player.getY(), player.getZ());
    }

    /**
     * Get the player's view yaw.
     *
     * @return yaw
     */
    @Override
    public double getYaw() {
        return player.getRotation();
    }

    /**
     * Gives the player an item.
     *
     * @param type
     * @param amt
     */
    @Override
    public void giveItem(int type, int amt) {
        player.giveItem(type, amt);
    }

    /**
     * Print a message.
     *
     * @param msg
     */
    @Override
    public void printRaw(String msg) {
        player.sendMessage(msg);
    }

    /**
     * Print a CraftBook message.
     *
     * @param msg
     */
    @Override
    public void print(String msg) {
        player.sendMessage(Colors.Gold + msg);
    }

    /**
     * Print a CraftBook error.
     *
     * @param msg
     */
    @Override
    public void printError(String msg) {
        player.sendMessage(Colors.Rose + msg);
    }

    /**
     * Move the player.
     *
     * @param pos
     * @param pitch
     * @param yaw
     */
    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        Location loc = new Location();
        loc.x = pos.getX();
        loc.y = pos.getY();
        loc.z = pos.getZ();
        loc.rotX = (float) yaw;
        loc.rotY = (float) pitch;
        player.teleportTo(loc);
    }

    /**
     * @return the player
     */
    public Player getPlayerObject() {
        return player;
    }

    /**
     * Checks if the user has a permission.
     * 
     * @name permission
     * @return
     */
    public boolean hasPermission(String permission) {
        return player.canUseCommand("/" + permission);
    }
}
