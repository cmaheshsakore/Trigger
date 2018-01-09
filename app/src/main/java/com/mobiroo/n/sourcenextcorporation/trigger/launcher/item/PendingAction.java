package com.mobiroo.n.sourcenextcorporation.trigger.launcher.item;

import android.view.View;
import com.mobiroo.n.sourcenextcorporation.trigger.launcher.action.Action;

public class PendingAction {
    public View InflatedView;
    public String Code;
    public String Name;
    public Action Action;
    
    public PendingAction(View view, String code) {
        this.InflatedView = view;
        this.Code = code;
    }

    public PendingAction(View view, String code, String name) {
        this.InflatedView = view;
        this.Code = code;
        this.Name = name;
    }
    
    public PendingAction(View view, String code, Action action) {
        this.InflatedView = view;
        this.Code = code;
        this.Action = action;
    }

    public PendingAction(View view, String code, Action action, String name) {
        this.InflatedView = view;
        this.Code = code;
        this.Name = name;
        this.Action = action;
    }
    
    
}
