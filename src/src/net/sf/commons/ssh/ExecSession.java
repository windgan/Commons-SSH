/*
 * Copyright 2009-2009 CommonsSSH Project.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.sf.commons.ssh;

/**
 * @author Sergey Vidyuk (svidyuk at gmail dot com)
 * @since 1.0
 * @see Feature#SESSION_EXEC
 */
@Deprecated
public interface ExecSession extends ShellSession {

    /**
     * Returns command exit status
     * 
     * @return command exit status, or <code>null</code> if it's not available
     */
    public abstract Integer getExitStatus();

}
