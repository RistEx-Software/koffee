/* 
 *  Koffee - A simple collection of utilities I commonly use
 *  Copyright (C) 2019-2020 Justin Crawford <justin@Stacksmash.net>
 *  Copyright (C) 2019-2020 Zachery Coleman <Zachery@Stacksmash.net>
 *  Copyright (C) 2019-2020 Skye Elliot <actuallyori@gmail.com>
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ristexsoftware.koffee.minecraft.bukkit.util;

import com.ristexsoftware.koffee.common.util.NumberUtil;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Various methods for teleporting entites in a minecraft world
 */
public class TeleportUtil {
    
    /**
     * Teleport the player to a random location in the world
     * @param player The player to teleport
     * @param maxX The max X coord range
     * @param maxZ The max Z coord range
     * <p>WARNING: This is unsafe! It may teleport player to dangerous locations (Like lava)
     */
    public static void teleportRandom(Player player, Integer maxX, Integer minX, Integer maxZ, Integer minZ) {
        int x = NumberUtil.getRandomNumber(minX, maxX);
        int z = NumberUtil.getRandomNumber(minZ, maxZ);
        player.teleport(new Location(player.getWorld(), x, player.getWorld().getHighestBlockYAt(x, z), z));
    }
}
