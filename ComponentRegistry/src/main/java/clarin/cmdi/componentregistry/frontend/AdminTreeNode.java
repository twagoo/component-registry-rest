/*
 * Copyright (C) 2020 CLARIN ERIC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package clarin.cmdi.componentregistry.frontend;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Twan Goosen <twan@clarin.eu>
 */
public class AdminTreeNode extends DefaultMutableTreeNode {

    public AdminTreeNode() {
    }

    public AdminTreeNode(DisplayDataNode userObject) {
        super(userObject);
    }

    public AdminTreeNode(DisplayDataNode userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    @Override
    public DisplayDataNode getUserObject() {
        return (DisplayDataNode) userObject;
    }

    @Override
    public boolean equals(Object obj) {
        if (userObject instanceof DisplayDataNode && obj instanceof AdminTreeNode) {
            return userObject.equals(((AdminTreeNode)obj).userObject);
        } else {
            return super.equals(obj);
        }
    }

    @Override
    public int hashCode() {
        if (userObject instanceof DisplayDataNode) {
            return userObject.hashCode();
        } else {
            return super.hashCode();
        }
    }

}
