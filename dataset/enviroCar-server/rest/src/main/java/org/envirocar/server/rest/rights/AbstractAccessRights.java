/*
 * Copyright (C) 2013-2022 The enviroCar project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.envirocar.server.rest.rights;

import com.google.common.collect.Maps;
import org.envirocar.server.core.FriendService;
import org.envirocar.server.core.GroupService;
import org.envirocar.server.core.entities.Group;
import org.envirocar.server.core.entities.User;

import java.util.Map;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann <autermann@uni-muenster.de>
 */
public abstract class AbstractAccessRights implements AccessRights {
    private final User user;
    private final GroupService groupService;
    private final FriendService friendService;
    private final Map<User, Boolean> isFriend = Maps.newHashMap();
    private final Map<User, Boolean> isFriendOf = Maps.newHashMap();
    private final Map<Group, Boolean> isMember = Maps.newHashMap();
    private final Map<User, Boolean> shareGroup = Maps.newHashMap();

    public AbstractAccessRights() {
        this(null, null, null);
    }

    public AbstractAccessRights(User user,
                                GroupService groupService,
                                FriendService friendService) {
        this.user = user;
        this.groupService = groupService;
        this.friendService = friendService;
    }

    @Override
    public boolean isSelf(User user) {
        if (this.user == null || user == null) {
            return false;
        }
        return user.equals(this.user);
    }

    @Override
    public boolean isAuthenticated() {
        return this.user != null;
    }

    protected boolean isFriend(User user) {
        if (this.user == null || user == null || this.friendService == null) {
            return false;
        }
        if (!this.isFriend.containsKey(user)) {
            this.isFriend.put(user, this.friendService.isFriend(this.user, user));
        }
        return this.isFriend.get(user);
    }

    protected boolean isFriendOf(User user) {
        if (this.user == null || user == null || this.friendService == null) {
            return false;
        }
        if (!this.isFriendOf.containsKey(user)) {
            this.isFriendOf.put(user, this.friendService.isFriend(user, this.user));
        }
        return this.isFriendOf.get(user);
    }

    protected boolean shareGroup(User user) {
        if (this.user == null || user == null || this.groupService == null) {
            return false;
        }
        if (!this.shareGroup.containsKey(user)) {
            this.shareGroup.put(user, this.groupService.shareGroup(this.user, user));
        }
        return this.shareGroup.get(user);
    }

    protected boolean isMember(Group group) {
        if (this.user == null || group == null || this.groupService == null) {
            return false;
        }
        if (!this.isMember.containsKey(group)) {
            this.isMember.put(group, this.groupService.isGroupMember(group, this.user));
        }
        return this.isMember.get(group);
    }

    protected boolean isSelfFriendOfOrShareGroup(User user) {
        return isSelf(user) || isFriendOf(user) || shareGroup(user);
    }


}
