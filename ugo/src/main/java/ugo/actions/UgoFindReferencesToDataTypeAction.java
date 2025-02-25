package ugo.actions;

/* ###
 * IP: GHIDRA
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

import docking.ActionContext;
import docking.action.MenuData;
import ghidra.app.actions.AbstractFindReferencesDataTypeAction;
import ghidra.app.decompiler.ClangFieldToken;
import ghidra.app.decompiler.ClangToken;
import ghidra.app.decompiler.component.DecompilerUtils;
import ghidra.app.plugin.core.decompile.DecompilerActionContext;
import ghidra.app.plugin.core.navigation.locationreferences.LocationReferencesService;
import ghidra.framework.plugintool.PluginTool;
import ghidra.program.model.data.DataType;
import ugo.UgoDecompilerController;
import ugo.UgoDecompilerPanel;

public class UgoFindReferencesToDataTypeAction extends AbstractFindReferencesDataTypeAction {

    private final UgoDecompilerController controller;

    public UgoFindReferencesToDataTypeAction(String owner, PluginTool tool,
                                             UgoDecompilerController controller) {
        super(tool, NAME, owner, DEFAULT_KEY_STROKE);
        this.controller = controller;

        setPopupMenuData(
                new MenuData(new String[]{LocationReferencesService.MENU_GROUP, "Find Uses of "}));
    }

    @Override
    public DataType getDataType(ActionContext context) {

        return DecompilerUtils.getDataType((DecompilerActionContext) context);
    }

    @Override
    protected String getDataTypeField() {

        UgoDecompilerPanel decompilerPanel = controller.getDecompilerPanel();
        ClangToken tokenAtCursor = decompilerPanel.getTokenAtCursor();
        if (tokenAtCursor instanceof ClangFieldToken) {
            return tokenAtCursor.getText();
        }

        return null;
    }

    @Override
    public boolean isEnabledForContext(ActionContext context) {
        if (!(context instanceof DecompilerActionContext)) {
            return false;
        }

        DecompilerActionContext decompilerContext = (DecompilerActionContext) context;
        return decompilerContext.checkActionEnablement(() -> {

            DataType dataType = getDataType(context);
            updateMenuName(dataType);

            return super.isEnabledForContext(context);
        });
    }

    @Override
    public void actionPerformed(ActionContext context) {

        DecompilerActionContext decompilerContext = (DecompilerActionContext) context;
        decompilerContext.performAction(() -> {
            super.actionPerformed(context);
        });
    }

    private void updateMenuName(DataType type) {

        if (type == null) {
            return; // not sure if this can happen
        }

        String typeName = type.getName();
        String menuName = "Find Uses of " + typeName;

        String fieldName = getDataTypeField();
        if (fieldName != null) {
            menuName += '.' + fieldName;
        }

        MenuData data = getPopupMenuData().cloneData();
        data.setMenuPath(new String[]{LocationReferencesService.MENU_GROUP, menuName});
        setPopupMenuData(data);
    }
}
