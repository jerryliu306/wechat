package com.kingston.logic.friend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.kingston.base.ClientBaseService;
import com.kingston.logic.friend.vo.FriendItemVo;
import com.kingston.ui.R;
import com.kingston.ui.StageController;

import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class FriendManager {

	private static FriendManager instance = new FriendManager();

	private Map<Long, FriendItemVo> friends = new HashMap<>();

	private Map<Integer, String> groupNames = new HashMap<>();
	/** 分组好友视图 */
	private TreeMap<Integer, List<FriendItemVo>> groupFriends = new TreeMap<>();

	public static FriendManager getInstance() {
		return instance;
	}

	public void receiveFriendsList(List<FriendItemVo> friendItems) {
		friends.clear();
		for (FriendItemVo item:friendItems) {
			friends.put(item.getUserId(), item);
		}
		rangeToGroupFriends(friendItems);

		ClientBaseService.INSTANCE.runTaskInFxThread(() -> {
			refreshMyFriendsView(friendItems);
		});

	}

	public FriendItemVo queryFriend(long friendId) {
		return this.friends.get(friendId);
	}

	public void refreshMyFriendsView(List<FriendItemVo> friendItems) {
		StageController stageController = ClientBaseService.INSTANCE.getStageController();
		Stage stage = stageController.getStageBy(R.id.MainView);
		ScrollPane scrollPane = (ScrollPane)stage.getScene().getRoot().lookup("#friendSp");
		Accordion friendGroup = (Accordion)scrollPane.getContent();
		friendGroup.getPanes().clear();

		for (Map.Entry<Integer, List<FriendItemVo>> entry:groupFriends.entrySet()) {
			int groupId = entry.getKey();
			String groupName = this.groupNames.get(groupId);
			decorateFriendGroup(friendGroup, groupName, entry.getValue());
		}
	}

	/**
	 * 调整成好友分组结构
	 */
	private void rangeToGroupFriends(List<FriendItemVo> friendItems) {
		this.groupFriends.clear();
		TreeMap<Integer, List<FriendItemVo>> groupFriends = new TreeMap<>();
		for (FriendItemVo item:friendItems) {
			int groupId= item.getGroup();
			List<FriendItemVo> frendsByGroup = groupFriends.get(groupId);
			if (frendsByGroup == null) {
				frendsByGroup = new ArrayList<>();
				groupFriends.put(groupId, frendsByGroup);
			}
			this.groupNames.put(groupId, item.getGroupName());
			frendsByGroup.add(item);
		}
		this.groupFriends = groupFriends;
	}


	private void decorateFriendGroup(Accordion container, String groupName, List<FriendItemVo> friendItems) {
		ListView<Node> listView = new ListView<Node>();
		int onlineCount = 0;
		StageController stageController = ClientBaseService.INSTANCE.getStageController();
		for (FriendItemVo item:friendItems) {
			Pane pane = stageController.load(R.layout.FriendItem, Pane.class);
			decorateFriendItem(pane, item);
			listView.getItems().add(pane);
		}

		String groupInfo = groupName + " " + onlineCount+"/"+friendItems.size();
		TitledPane tp = new TitledPane(groupInfo, listView);
		container.getPanes().add(tp);
	}

	private void decorateFriendItem(Pane itemUi, FriendItemVo friendVo) {
		Label autographLabel = (Label) itemUi.lookup("#signature");
		autographLabel.setText(friendVo.getSignature());
		Hyperlink _username_ = (Hyperlink) itemUi.lookup("#userName");
		_username_.setText(friendVo.getFullName());
	}

}
