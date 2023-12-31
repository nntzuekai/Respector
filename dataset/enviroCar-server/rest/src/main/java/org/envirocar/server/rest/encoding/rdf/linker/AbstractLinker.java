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
package org.envirocar.server.rest.encoding.rdf.linker;

import javax.ws.rs.core.UriBuilder;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.envirocar.server.rest.encoding.rdf.RDFLinker;
import org.envirocar.server.rest.rights.AccessRights;

import com.google.inject.Provider;

/**
 * TODO JavaDoc
 *
 * @author Christian Autermann
 */
public abstract class AbstractLinker<T> implements RDFLinker<T> {

    @Override
    public final void link(Model m, T t, AccessRights rights, Resource uri,
                           Provider<UriBuilder> uriBuilder) {
        addNamespaces(m);
        linkInternal(m, t, rights, uri, uriBuilder);
    }

    protected abstract void addNamespaces(Model m);

    protected abstract void linkInternal(Model model,
                                         T entity,
                                         AccessRights rights,
                                         Resource uri,
                                         Provider<UriBuilder> uriBuilder);

}
