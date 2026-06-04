package com.example.todolist.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.todolist.viewmodel.MainViewModel
import com.example.todolist.viewmodel.NoteViewModel
import com.example.todolist.viewmodel.TodoViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    mainViewModel: MainViewModel,
    todoViewModel: TodoViewModel,
    noteViewModel: NoteViewModel
) {
    val tabs = listOf("Tasks", "Notes")
    val initialPage by mainViewModel.selectedTabIndex.collectAsState()
    val pagerState = rememberPagerState(initialPage = initialPage) { tabs.size }
    val coroutineScope = rememberCoroutineScope()

    var isSearchActive by remember { mutableStateOf(false) }
    val todoSearchQuery by todoViewModel.searchQuery.collectAsState()
    val noteSearchQuery by noteViewModel.searchQuery.collectAsState()

    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            mainViewModel.onTabSelected(pagerState.currentPage)
            isSearchActive = false
            todoViewModel.onSearchQueryChange("")
            noteViewModel.onSearchQueryChange("")
        }
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.75f)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "NoDoList",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    style = MaterialTheme.typography.titleLarge
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Palette, contentDescription = "Change Theme") },
                    label = { Text("Change Theme") },
                    selected = false,
                    onClick = {
                        mainViewModel.toggleTheme()
                        coroutineScope.launch {
                            drawerState.close()
                        }
                    }
                )
                NavigationDrawerItem(
                    icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                    label = { Text("About") },
                    selected = false,
                    // Implement the onClick to navigate
                    onClick = {
                        coroutineScope.launch {
                            drawerState.close()
                        }
                        navController.navigate("about_screen")
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        TabRow(
                            selectedTabIndex = pagerState.currentPage,
                            containerColor = MaterialTheme.colorScheme.primary,
                            indicator = { tabPositions ->
                                TabRowDefaults.Indicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            },
                            divider = {}
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerState.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(title) },
                                    icon = {
                                        Icon(
                                            if (index == 0) Icons.Default.FormatListBulleted else Icons.Default.Description,
                                            contentDescription = title
                                        )
                                    },
                                    selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                    unselectedContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    actions = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                isSearchActive = false
                                if (pagerState.currentPage == 0) todoViewModel.onSearchQueryChange("")
                                else noteViewModel.onSearchQueryChange("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Search")
                            }
                        } else {
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        if (pagerState.currentPage == 0) {
                            todoViewModel.onAddTodoClicked()
                        } else {
                            navController.navigate("add_edit_note_screen/-1")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    AnimatedContent(
                        targetState = pagerState.currentPage,
                        transitionSpec = {
                            (slideInVertically { height -> height } + fadeIn())
                                .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                        },
                        label = "fabIcon"
                    ) { targetPage ->
                        if (targetPage == 0) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Task",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Add Note",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                AnimatedVisibility(visible = isSearchActive) {
                    OutlinedTextField(
                        value = if (pagerState.currentPage == 0) todoSearchQuery else noteSearchQuery,
                        onValueChange = {
                            if (pagerState.currentPage == 0) {
                                todoViewModel.onSearchQueryChange(it)
                            } else {
                                noteViewModel.onSearchQueryChange(it)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text(if (pagerState.currentPage == 0) "Search tasks..." else "Search notes...") },
                        maxLines = 1,
                        shape = CircleShape
                    )
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> TodoScreen(viewModel = todoViewModel)
                        1 -> NoteScreen(navController = navController, viewModel = noteViewModel)
                    }
                }
            }
        }
    }
}