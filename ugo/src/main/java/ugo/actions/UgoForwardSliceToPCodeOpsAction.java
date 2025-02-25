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

import docking.action.MenuData;
import ghidra.app.decompiler.ClangToken;
import ghidra.app.decompiler.component.DecompilerUtils;
import ghidra.app.plugin.core.decompile.DecompilerActionContext;
import ghidra.program.model.pcode.PcodeOp;
import ghidra.program.model.pcode.Varnode;
import ugo.UgoDecompilerController;
import ugo.UgoDecompilerPanel;

import java.util.Set;

public class UgoForwardSliceToPCodeOpsAction extends UgoAbstractDecompilerAction {
    private final UgoDecompilerController controller;

    public UgoForwardSliceToPCodeOpsAction(UgoDecompilerController controller) {
        super("Highlight Forward Inst Slice");
        this.controller = controller;
        setPopupMenuData(
                new MenuData(new String[]{"Highlight Forward Inst Slice"}, "Decompile"));
    }

    @Override
    protected boolean isEnabledForDecompilerContext(DecompilerActionContext context) {
        UgoDecompilerPanel decompilerPanel = controller.getDecompilerPanel();
        ClangToken tokenAtCursor = decompilerPanel.getTokenAtCursor();
        Varnode varnode = DecompilerUtils.getVarnodeRef(tokenAtCursor);
        return varnode != null;
    }

    @Override
    protected void decompilerActionPerformed(DecompilerActionContext context) {
        UgoDecompilerPanel decompilerPanel = controller.getDecompilerPanel();
        ClangToken tokenAtCursor = decompilerPanel.getTokenAtCursor();
        Varnode varnode = DecompilerUtils.getVarnodeRef(tokenAtCursor);
        if (varnode != null) {
            PcodeOp op = tokenAtCursor.getPcodeOp();
            Set<PcodeOp> forwardSlice = DecompilerUtils.getForwardSliceToPCodeOps(varnode);
            forwardSlice.add(op);
            decompilerPanel.clearHighlights();
            decompilerPanel.addPcodeOpHighlights(forwardSlice,
                    decompilerPanel.getDefaultHighlightColor());
            decompilerPanel.repaint();
        }
    }
}
