package com.example.onlineteach.ui.notifications;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.onlineteach.data.model.Group;
import com.example.onlineteach.data.repository.GroupRepository;
import com.example.onlineteach.data.repository.UserRepository;

import java.util.List;

public class NotificationsViewModel extends AndroidViewModel {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<String> toastMessage;
    private LiveData<List<Group>> myGroups;

    public NotificationsViewModel(Application application) {
        super(application);
        groupRepository = new GroupRepository(application);
        userRepository = new UserRepository(application);
        toastMessage = new MutableLiveData<>();
        loadMyGroups();
    }

    private void loadMyGroups() {
        int userId = userRepository.getLoggedInUserId();
        if (userId != -1) {
            myGroups = groupRepository.getJoinedGroups(userId);
        }
    }

    public LiveData<List<Group>> getMyGroups() {
        return myGroups;
    }

    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    public void clearToastMessage() {
        toastMessage.setValue(null);
    }
}