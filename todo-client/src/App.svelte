<script>
  // Svelte code based on https://freshman.tech/svelte-todo/
  // Modified to use the Quarkus / Postgres backend

  import { afterUpdate } from 'svelte';
  import { onMount } from "svelte";

  let todoItems = [];
  let newTodo = '';
  
  onMount(async function() {
    const response = await fetch("/todos");
    const json = await response.json();
    todoItems = json;
  });

  afterUpdate(() => {
    document.querySelector('.js-todo-input').focus();
  });


  async function addTodo() {
    newTodo = newTodo.trim();
    if (!newTodo) return;

    const response = await fetch("/todos", { method: 'POST', headers: {'Content-Type': 'application/json'}, body: newTodo });
    const todo = await response.json();

    todoItems = [...todoItems, todo];
    newTodo = '';
  }

  function toggleDone(id) {
    const index = todoItems.findIndex(item => item.id === Number(id));
    todoItems[index].completed = !todoItems[index].completed;
    update(todoItems[index])
  }

  async function update(todo) {

    const response = await fetch("/todos/"+todo.id, { method: 'PUT', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(todo) });
    const updatedTodo = await response.json();

    todo = updatedTodo;
  }

  async function deleteTodo(id) {
    const response = await fetch("/todos/"+id, { method: 'DELETE' });
    const success = await response.ok;

    if (success) todoItems = todoItems.filter(item => item.id !== Number(id));
  }
</script>

<main>
  <div class="container">
    <h1 class="app-title">todos</h1>
    
    
    <ul class="todo-list">
      {#each todoItems as todo (todo.id)}
        <li class="todo-item {todo.completed ? 'done' : ''}">
          <input id={todo.id} type="checkbox" />
          <label for={todo.id} class="tick" on:click={() => toggleDone(todo.id)}></label>
          <span>{todo.txt}</span>
          <button class="delete-todo" on:click={() => deleteTodo(todo.id)}>
            <svg><use href="#delete-icon"></use></svg>
          </button>
        </li>
      {/each}
    </ul>

    <div class="empty-state">
      <svg class="checklist-icon"><use href="#checklist-icon"></use></svg>
      <h2 class="empty-state__title">Add your first todo</h2>
      <p class="empty-state__description">What do you want to get done today?</p>
    </div>
    <form on:submit|preventDefault={addTodo}>
      <input class="js-todo-input" type="text" aria-label="Enter a new todo item" placeholder="E.g. Build a web app" bind:value={newTodo}>
    </form>
  </div>
</main>
